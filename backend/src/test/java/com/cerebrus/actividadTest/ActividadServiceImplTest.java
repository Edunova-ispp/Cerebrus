package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.ActividadServiceImpl;
import com.cerebrus.actividad.general.General;
import com.cerebrus.curso.Curso;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {
    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private TemaService temaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ActividadServiceImpl actividadService;

    @Captor
    private ArgumentCaptor<Actividad> actividadCaptor;

    @Test
    void crearActTeoria_temaNoExiste_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro(1L));
        when(temaService.encontrarTemaPorId(99L))
                .thenThrow(new IllegalArgumentException("Tema no encontrado con ID: 99"));

        assertThatThrownBy(() -> actividadService.crearActTeoria("Titulo", "Desc", null, 99L, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tema no encontrado");

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActTeoria_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});

        assertThatThrownBy(() -> actividadService.crearActTeoria("T", "D", null, 1L, false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActTeoria_maxPosicionNull_asignaPosicion1_yGuardaGeneralTeoria() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(55L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.encontrarTemaPorId(55L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(55L)).thenReturn(null);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        Actividad resultado = actividadService.crearActTeoria("Título", "Desc", null, 55L, false);

        assertThat(resultado).isNotNull();
        assertThat(resultado).isInstanceOf(General.class);

        verify(actividadRepository).save(actividadCaptor.capture());
        Actividad guardada = actividadCaptor.getValue();

        assertThat(guardada.getTitulo()).isEqualTo("Título");
        assertThat(guardada.getDescripcion()).isEqualTo("Desc");
        assertThat(guardada.getPuntuacion()).isEqualTo(1);
        assertThat(guardada.getImagen()).isNull();
        assertThat(guardada.getRespVisible()).isFalse();
        assertThat(guardada.getPosicion()).isEqualTo(1);
        assertThat(guardada.getVersion()).isEqualTo(1);
        assertThat(guardada.getTema()).isSameAs(tema);

        General general = (General) guardada;
        assertThat(general.getTipo()).isEqualTo(TipoActGeneral.TEORIA);
    }

    @Test
    void crearActTeoria_maxPosicion0_asignaPosicion1_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(56L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.encontrarTemaPorId(56L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(56L)).thenReturn(0);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActTeoria("T", "D", null, 56L, false);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(1);
    }

    @Test
    void crearActTeoria_maxPosicionMayor_asignaMaxMasUno_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(57L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.encontrarTemaPorId(57L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(57L)).thenReturn(12);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActTeoria("T", "D", null, 57L, false);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(13);
    }

    @Test
    void encontrarActividadesPorTema_listaVacia_devuelveVacio() {
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        List<Actividad> resultado = actividadService.encontrarActividadesPorTema(1L);

        assertThat(resultado).isEmpty();
        verify(actividadRepository).findByTemaId(1L);
    }

    @Test
    void encontrarActividadesPorTema_conElementos_devuelveMismaLista() {
        Actividad a1 = new Actividad() {};
        Actividad a2 = new Actividad() {};
        List<Actividad> lista = List.of(a1, a2);
        when(actividadRepository.findByTemaId(2L)).thenReturn(lista);

        List<Actividad> resultado = actividadService.encontrarActividadesPorTema(2L);

        assertThat(resultado).containsExactly(a1, a2);
        verify(actividadRepository).findByTemaId(2L);
    }

        @Test
    void encontrarActTeoriaMaestroPorId_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.encontrarActTeoriaMaestroPorId(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("No tienes permiso");
    }

    @Test
    void encontrarActTeoriaMaestroPorId_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.encontrarActTeoriaMaestroPorId(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no es tuyo");
    }

    @Test
    void encontrarActTeoriaMaestroPorId_maestroPropietario_devuelveActividad() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        Actividad result = actividadService.encontrarActTeoriaMaestroPorId(5L);
        assertThat(result).isSameAs(actividad);
    }

    @Test
    void encontrarActTeoriaPorId_usuarioNoEsAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.encontrarActTeoriaPorId(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("No tienes permiso");
    }

    @Test
    void encontrarActTeoriaPorId_alumnoNoInscrito_lanzaAccessDeniedException() {
        com.cerebrus.usuario.alumno.Alumno alumno = new com.cerebrus.usuario.alumno.Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        // No inscripciones
        curso.setInscripciones(List.of());
        assertThatThrownBy(() -> actividadService.encontrarActTeoriaPorId(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no estás inscrito");
    }

    @Test
    void encontrarActTeoriaPorId_cursoOculto_lanzaAccessDeniedException() {
        com.cerebrus.usuario.alumno.Alumno alumno = new com.cerebrus.usuario.alumno.Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Curso curso = crearCurso(maestro);
        curso.setVisibilidad(false);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        com.cerebrus.inscripcion.Inscripcion inscripcion = new com.cerebrus.inscripcion.Inscripcion();
        inscripcion.setAlumno(alumno);
        curso.setInscripciones(List.of(inscripcion));

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));

        assertThatThrownBy(() -> actividadService.encontrarActTeoriaPorId(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("curso oculto");
    }

    @Test
    void encontrarActTeoriaPorId_alumnoInscrito_devuelveActividad() {
        com.cerebrus.usuario.alumno.Alumno alumno = new com.cerebrus.usuario.alumno.Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        com.cerebrus.inscripcion.Inscripcion inscripcion = new com.cerebrus.inscripcion.Inscripcion();
        inscripcion.setAlumno(alumno);
        curso.setInscripciones(List.of(inscripcion));
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        Actividad result = actividadService.encontrarActTeoriaPorId(5L);
        assertThat(result).isSameAs(actividad);
    }

    @Test
    void encontrarActTeoriaPorId_actividadFuturaBloqueada_lanzaAccessDeniedException() {
        com.cerebrus.usuario.alumno.Alumno alumno = new com.cerebrus.usuario.alumno.Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);

        Actividad actividad1 = new General();
        actividad1.setId(100L);
        actividad1.setPosicion(1);
        actividad1.setTema(tema);

        Actividad actividad2 = new General();
        actividad2.setId(200L);
        actividad2.setPosicion(2);
        actividad2.setTema(tema);

        tema.setActividades(List.of(actividad1, actividad2));

        com.cerebrus.inscripcion.Inscripcion inscripcion = new com.cerebrus.inscripcion.Inscripcion();
        inscripcion.setAlumno(alumno);
        curso.setInscripciones(List.of(inscripcion));

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(200L)).thenReturn(java.util.Optional.of(actividad2));

        assertThatThrownBy(() -> actividadService.encontrarActTeoriaPorId(200L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("todavía no está desbloqueada");
    }

    @Test
    void encontrarActTeoriaPorId_siguienteDesbloqueadaSiAnteriorTerminada_devuelveActividad() {
        com.cerebrus.usuario.alumno.Alumno alumno = new com.cerebrus.usuario.alumno.Alumno();
        alumno.setId(1L);
        Maestro maestro = crearMaestro(2L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);

        Actividad actividad1 = new General();
        actividad1.setId(100L);
        actividad1.setPosicion(1);
        actividad1.setTema(tema);

        ActividadAlumno progresoActividad1 = new ActividadAlumno();
        progresoActividad1.setAlumno(alumno);
        progresoActividad1.setFechaFin(LocalDateTime.now());
        actividad1.setActividadesAlumno(List.of(progresoActividad1));

        Actividad actividad2 = new General();
        actividad2.setId(200L);
        actividad2.setPosicion(2);
        actividad2.setTema(tema);

        tema.setActividades(List.of(actividad1, actividad2));

        com.cerebrus.inscripcion.Inscripcion inscripcion = new com.cerebrus.inscripcion.Inscripcion();
        inscripcion.setAlumno(alumno);
        curso.setInscripciones(List.of(inscripcion));

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(200L)).thenReturn(java.util.Optional.of(actividad2));

        Actividad result = actividadService.encontrarActTeoriaPorId(200L);
        assertThat(result).isSameAs(actividad2);
    }

    @Test
    void eliminarActTeoriaPorId_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.eliminarActTeoriaPorId(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void eliminarActTeoriaPorId_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.eliminarActTeoriaPorId(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no son tuyos");
    }

    @Test
    void eliminarActTeoriaPorId_maestroPropietario_eliminaActividad() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        actividadService.eliminarActTeoriaPorId(5L);
        verify(actividadRepository).delete(actividad);
    }

    @Test
    void actualizarActTeoria_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.actualizarActTeoria(1L, "T", "D", null, false))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void actualizarActTeoria_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.actualizarActTeoria(5L, "T", "D", null, false))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no son tuyos");
    }

    @Test
    void actualizarActTeoria_maestroPropietario_actualizaYGuarda() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));
        Actividad result = actividadService.actualizarActTeoria(5L, "Nuevo T", "Nueva D", "img.png", false);
        assertThat(result.getTitulo()).isEqualTo("Nuevo T");
        assertThat(result.getDescripcion()).isEqualTo("Nueva D");
        assertThat(result.getImagen()).isEqualTo("img.png");
        verify(actividadRepository).save(actividad);
    }

    private static Maestro crearMaestro(Long id) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        return maestro;
    }

    private static Curso crearCurso(Maestro maestro) {
        Curso curso = new Curso();
        curso.setMaestro(maestro);
        curso.setVisibilidad(true);
        return curso;
    }

    private static Tema crearTema(Long id, Curso curso) {
        Tema tema = new Tema();
        tema.setId(id);
        tema.setCurso(curso);
        return tema;
    }
}