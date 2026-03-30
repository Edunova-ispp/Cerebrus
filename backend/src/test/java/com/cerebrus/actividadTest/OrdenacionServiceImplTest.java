package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionRepository;
import com.cerebrus.actividad.ordenacion.OrdenacionServiceImpl;
import com.cerebrus.curso.Curso;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class OrdenacionServiceImplTest {

    @Mock
    private OrdenacionRepository ordenacionRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OrdenacionServiceImpl ordenacionService;

    private Maestro maestro;
    private Alumno alumno;
    private Usuario usuarioNoMaestro;
    private Ordenacion ordenacion;
    private List<String> valores;
    private Tema tema;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        maestro.setId(2L);
        usuarioNoMaestro = new Usuario() {};

        alumno = new Alumno();
		alumno.setId(1L);

        Inscripcion inscripcion = new Inscripcion();
		inscripcion.setAlumno(alumno);

		List<Inscripcion> inscripciones = new LinkedList<>();
		inscripciones.add(inscripcion);

        // Tema de prueba existente (necesario porque el servicio valida que temaId exista)
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setTitulo("Curso test");
        curso.setCodigo("COD-TEST");
        curso.setVisibilidad(true);
        curso.setMaestro(maestro);
        curso.setInscripciones(inscripciones);

        tema = new Tema();
        tema.setId(1L);
        tema.setTitulo("Tema test");
        tema.setCurso(curso);

        // Hay tests que no tocan TemaRepository (read/delete/access denied), así que este stub debe ser lenient.
        lenient().when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        valores = new ArrayList<>(List.of("Primero", "Segundo", "Tercero"));

        ordenacion = new Ordenacion();
        ordenacion.setId(10L);
        ordenacion.setTitulo("Ordena los planetas");
        ordenacion.setDescripcion("Descripción de prueba");
        ordenacion.setPuntuacion(100);
        ordenacion.setImagen("img.png");
        ordenacion.setRespVisible(false);
        ordenacion.setPosicion(1);
        ordenacion.setVersion(1);
        ordenacion.setValores(new ArrayList<>(valores));
        ordenacion.setTema(tema);
    }

    // -------------------------------------------------------
    // crearActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que crearActOrdenacion guarda correctamente cuando el usuario es Maestro y respVisible es false
    @Test
    void crearActOrdenacion_maestroRespNoVisible_guardaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.crearActOrdenacion(
                "Ordena los planetas", "Desc", 100, "img.png", 1L,
                false, null, 1, valores);

        assertThat(resultado.getTitulo()).isEqualTo("Ordena los planetas");
        assertThat(resultado.getPuntuacion()).isEqualTo(100);
        assertThat(resultado.getRespVisible()).isFalse();
        assertThat(resultado.getComentariosRespVisible()).isNull();
        assertThat(resultado.getVersion()).isEqualTo(1);
        assertThat(resultado.getValores()).containsExactlyElementsOf(valores);
        verify(ordenacionRepository).save(any(Ordenacion.class));
    }

    // Test para verificar que crearActOrdenacion asigna comentariosRespVisible cuando respVisible es true
    @Test
    void crearActOrdenacion_maestroRespVisible_asignaComentarios() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.crearActOrdenacion(
                "Ordena los planetas", "Desc", 100, "img.png", 1L,
                true, "Comentario de ejemplo", 1, valores);

        assertThat(resultado.getRespVisible()).isTrue();
        assertThat(resultado.getComentariosRespVisible()).isEqualTo("Comentario de ejemplo");
    }

    // Test para verificar que crearActOrdenacion lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void crearActOrdenacion_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> ordenacionService.crearActOrdenacion(
                "Título", "Desc", 50, null, 1L, false, null, 1, valores))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear actividades de ordenación");

        verify(ordenacionRepository, never()).save(any());
    }

    // Test para verificar que crearActOrdenacion guarda correctamente cuando la lista de valores tiene un solo elemento (caso límite)
    @Test
    void crearActOrdenacion_unSoloValor_seGuardaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.crearActOrdenacion(
                "Título", "Desc", 50, null, 1L, false, null, 1, List.of("Único"));

        assertThat(resultado.getValores()).hasSize(1);
    }

    // Test para verificar que crearActOrdenacion guarda correctamente cuando imagen y descripción son null (caso límite)
    @Test
    void crearActOrdenacion_imagenYDescripcionNull_seGuardaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.crearActOrdenacion(
                "Título", null, 50, null, 1L, false, null, 1, valores);

        assertThat(resultado.getDescripcion()).isNull();
        assertThat(resultado.getImagen()).isNull();
    }

    // -------------------------------------------------------
    // encontrarActOrdenacionPorId
    // -------------------------------------------------------

    // Test para verificar que encontrarActOrdenacionPorId retorna la ordenación con sus valores (posiblemente reordenados por shuffle)
    @Test
    void encontrarActOrdenacionPorId_existente_retornaOrdenacionConValores() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.encontrarActOrdenacionPorId(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getValores()).containsExactlyInAnyOrder("Primero", "Segundo", "Tercero");
    }

    // Test para verificar que encontrarActOrdenacionPorId retorna la ordenación con lista de valores vacía cuando no tiene valores
    @Test
    void encontrarActOrdenacionPorId_sinValores_retornaListaVacia() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        ordenacion.setValores(new ArrayList<>());
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.encontrarActOrdenacionPorId(10L);

        assertThat(resultado.getValores()).isEmpty();
    }

    // Test para verificar que encontrarActOrdenacionPorId retorna el único valor sin modificaciones (caso límite de shuffle)
    @Test
    void encontrarActOrdenacionPorId_unSoloValor_retornaMismoValor() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        ordenacion.setValores(new ArrayList<>(List.of("Único")));
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.encontrarActOrdenacionPorId(10L);

        assertThat(resultado.getValores()).containsExactly("Único");
    }

    // Test para verificar que encontrarActOrdenacionPorId lanza RuntimeException cuando la ordenación no existe
    @Test
    void encontrarActOrdenacionPorId_noExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(ordenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenacionService.encontrarActOrdenacionPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");
    }

    @Test
    void encontrarActOrdenacionPorId_cursoOculto_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        tema.getCurso().setVisibilidad(false);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        assertThatThrownBy(() -> ordenacionService.encontrarActOrdenacionPorId(10L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("curso oculto");
    }

    // -------------------------------------------------------
    // actualizarActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que actualizarActOrdenacion actualiza todos los campos correctamente cuando el usuario es Maestro
    @Test
    void actualizarActOrdenacion_maestro_actualizaCamposCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> nuevosValores = List.of("A", "B", "C");
        Ordenacion resultado = ordenacionService.actualizarActOrdenacion(
                10L, "Nuevo título", "Nueva desc", 200, "nueva.png",
                1L, false, null, 2, nuevosValores);

        assertThat(resultado.getTitulo()).isEqualTo("Nuevo título");
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva desc");
        assertThat(resultado.getPuntuacion()).isEqualTo(200);
        assertThat(resultado.getImagen()).isEqualTo("nueva.png");
        assertThat(resultado.getPosicion()).isEqualTo(2);
        assertThat(resultado.getValores()).containsExactlyElementsOf(nuevosValores);
        assertThat(resultado.getVersion()).isEqualTo(2);
        verify(ordenacionRepository).save(ordenacion);
    }

    // Test para verificar que actualizarActOrdenacion incrementa la versión en 1 en cada actualización
    @Test
    void actualizarActOrdenacion_maestro_incrementaVersionEnUno() {
        ordenacion.setVersion(3);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.actualizarActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, null, 1, valores);

        assertThat(resultado.getVersion()).isEqualTo(4);
    }

    // Test para verificar que actualizarActOrdenacion asigna comentariosRespVisible cuando respVisible es true
    @Test
    void actualizarActOrdenacion_respVisible_asignaComentarios() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.actualizarActOrdenacion(
                10L, "T", "D", 50, null, 1L, true, "Nuevo comentario", 1, valores);

        assertThat(resultado.getRespVisible()).isTrue();
        assertThat(resultado.getComentariosRespVisible()).isEqualTo("Nuevo comentario");
    }

    // Test para verificar que actualizarActOrdenacion pone comentariosRespVisible a null cuando respVisible es false
    @Test
    void actualizarActOrdenacion_respNoVisible_comentariosNull() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.actualizarActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, "ignorado", 1, valores);

        assertThat(resultado.getRespVisible()).isFalse();
        assertThat(resultado.getComentariosRespVisible()).isNull();
    }

    // Test para verificar que actualizarActOrdenacion lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void actualizarActOrdenacion_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> ordenacionService.actualizarActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, null, 1, valores))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar actividades de ordenación");

        verify(ordenacionRepository, never()).save(any());
        verify(ordenacionRepository, never()).findById(any());
    }

    // Test para verificar que actualizarActOrdenacion lanza RuntimeException cuando la ordenación no existe
    @Test
    void actualizarActOrdenacion_noExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenacionService.actualizarActOrdenacion(
                99L, "T", "D", 50, null, 1L, false, null, 1, valores))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");

        verify(ordenacionRepository, never()).save(any());
    }

    // -------------------------------------------------------
    // eliminarActOrdenacionPorId
    // -------------------------------------------------------

    // Test para verificar que eliminarActOrdenacionPorId elimina correctamente cuando el usuario es Maestro
    @Test
    void eliminarActOrdenacionPorId_maestro_eliminaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        ordenacionService.eliminarActOrdenacionPorId(10L);

        verify(ordenacionRepository).deleteById(10L);
    }

    // Test para verificar que eliminarActOrdenacionPorId lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void eliminarActOrdenacionPorId_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> ordenacionService.eliminarActOrdenacionPorId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar actividades de ordenación");

        verify(ordenacionRepository, never()).deleteById(any());
    }
}
