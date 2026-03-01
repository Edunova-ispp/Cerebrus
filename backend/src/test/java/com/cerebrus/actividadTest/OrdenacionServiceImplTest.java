package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Ordenacion;
import com.cerebrus.actividad.OrdenacionRepository;
import com.cerebrus.actividad.OrdenacionServiceImpl;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class OrdenacionServiceImplTest {

    @Mock
    private OrdenacionRepository ordenacionRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OrdenacionServiceImpl ordenacionService;

    private Maestro maestro;
    private Usuario usuarioNoMaestro;
    private Ordenacion ordenacion;
    private List<String> valores;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        usuarioNoMaestro = new Usuario() {};

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
    // readOrdenacion
    // -------------------------------------------------------

    // Test para verificar que readOrdenacion retorna la ordenación con sus valores (posiblemente reordenados por shuffle)
    @Test
    void readOrdenacion_existente_retornaOrdenacionConValores() {
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.readOrdenacion(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getValores()).containsExactlyInAnyOrder("Primero", "Segundo", "Tercero");
    }

    // Test para verificar que readOrdenacion retorna la ordenación con lista de valores vacía cuando no tiene valores
    @Test
    void readOrdenacion_sinValores_retornaListaVacia() {
        ordenacion.setValores(new ArrayList<>());
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.readOrdenacion(10L);

        assertThat(resultado.getValores()).isEmpty();
    }

    // Test para verificar que readOrdenacion retorna el único valor sin modificaciones (caso límite de shuffle)
    @Test
    void readOrdenacion_unSoloValor_retornaMismoValor() {
        ordenacion.setValores(new ArrayList<>(List.of("Único")));
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));

        Ordenacion resultado = ordenacionService.readOrdenacion(10L);

        assertThat(resultado.getValores()).containsExactly("Único");
    }

    // Test para verificar que readOrdenacion lanza RuntimeException cuando la ordenación no existe
    @Test
    void readOrdenacion_noExiste_lanzaRuntimeException() {
        when(ordenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenacionService.readOrdenacion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");
    }

    // -------------------------------------------------------
    // updateActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que updateActOrdenacion actualiza todos los campos correctamente cuando el usuario es Maestro
    @Test
    void updateActOrdenacion_maestro_actualizaCamposCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> nuevosValores = List.of("A", "B", "C");
        Ordenacion resultado = ordenacionService.updateActOrdenacion(
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

    // Test para verificar que updateActOrdenacion incrementa la versión en 1 en cada actualización
    @Test
    void updateActOrdenacion_maestro_incrementaVersionEnUno() {
        ordenacion.setVersion(3);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.updateActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, null, 1, valores);

        assertThat(resultado.getVersion()).isEqualTo(4);
    }

    // Test para verificar que updateActOrdenacion asigna comentariosRespVisible cuando respVisible es true
    @Test
    void updateActOrdenacion_respVisible_asignaComentarios() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.updateActOrdenacion(
                10L, "T", "D", 50, null, 1L, true, "Nuevo comentario", 1, valores);

        assertThat(resultado.getRespVisible()).isTrue();
        assertThat(resultado.getComentariosRespVisible()).isEqualTo("Nuevo comentario");
    }

    // Test para verificar que updateActOrdenacion pone comentariosRespVisible a null cuando respVisible es false
    @Test
    void updateActOrdenacion_respNoVisible_comentariosNull() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(10L)).thenReturn(Optional.of(ordenacion));
        when(ordenacionRepository.save(any(Ordenacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordenacion resultado = ordenacionService.updateActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, "ignorado", 1, valores);

        assertThat(resultado.getRespVisible()).isFalse();
        assertThat(resultado.getComentariosRespVisible()).isNull();
    }

    // Test para verificar que updateActOrdenacion lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void updateActOrdenacion_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> ordenacionService.updateActOrdenacion(
                10L, "T", "D", 50, null, 1L, false, null, 1, valores))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar actividades de ordenación");

        verify(ordenacionRepository, never()).save(any());
        verify(ordenacionRepository, never()).findById(any());
    }

    // Test para verificar que updateActOrdenacion lanza RuntimeException cuando la ordenación no existe
    @Test
    void updateActOrdenacion_noExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(ordenacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenacionService.updateActOrdenacion(
                99L, "T", "D", 50, null, 1L, false, null, 1, valores))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");

        verify(ordenacionRepository, never()).save(any());
    }

    // -------------------------------------------------------
    // deleteActOrdenacion
    // -------------------------------------------------------

    // Test para verificar que deleteActOrdenacion elimina correctamente cuando el usuario es Maestro
    @Test
    void deleteActOrdenacion_maestro_eliminaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        ordenacionService.deleteActOrdenacion(10L);

        verify(ordenacionRepository).deleteById(10L);
    }

    // Test para verificar que deleteActOrdenacion lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void deleteActOrdenacion_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> ordenacionService.deleteActOrdenacion(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar actividades de ordenación");

        verify(ordenacionRepository, never()).deleteById(any());
    }
}
