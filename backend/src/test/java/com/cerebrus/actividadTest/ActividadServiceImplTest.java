package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
    void crearActividadTeoria_temaNoExiste_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro(1L));
        when(temaService.obtenerTemaPorId(99L))
                .thenThrow(new IllegalArgumentException("Tema no encontrado con ID: 99"));

        assertThatThrownBy(() -> actividadService.crearActividadTeoria("Titulo", "Desc", null, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tema no encontrado");

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActividadTeoria_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});

        assertThatThrownBy(() -> actividadService.crearActividadTeoria("T", "D", null, 1L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActividadTeoria_maxPosicionNull_asignaPosicion1_yGuardaGeneralTeoria() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(55L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(55L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(55L)).thenReturn(null);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        Actividad resultado = actividadService.crearActividadTeoria("Título", "Desc", null, 55L);

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
    void crearActividadTeoria_maxPosicion0_asignaPosicion1_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(56L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(56L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(56L)).thenReturn(0);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActividadTeoria("T", "D", null, 56L);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(1);
    }

    @Test
    void crearActividadTeoria_maxPosicionMayor_asignaMaxMasUno_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(57L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(57L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(57L)).thenReturn(12);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActividadTeoria("T", "D", null, 57L);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(13);
    }

    @Test
    void obtenerActividadesPorTema_listaVacia_devuelveVacio() {
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        List<Actividad> resultado = actividadService.ObtenerActividadesPorTema(1L);

        assertThat(resultado).isEmpty();
        verify(actividadRepository).findByTemaId(1L);
    }

    @Test
    void obtenerActividadesPorTema_conElementos_devuelveMismaLista() {
        Actividad a1 = new Actividad() {};
        Actividad a2 = new Actividad() {};
        List<Actividad> lista = List.of(a1, a2);
        when(actividadRepository.findByTemaId(2L)).thenReturn(lista);

        List<Actividad> resultado = actividadService.ObtenerActividadesPorTema(2L);

        assertThat(resultado).containsExactly(a1, a2);
        verify(actividadRepository).findByTemaId(2L);
    }

        @Test
    void encontrarActividadPorIdMaestro_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.encontrarActividadPorIdMaestro(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("No tienes permiso");
    }

    @Test
    void encontrarActividadPorIdMaestro_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.encontrarActividadPorIdMaestro(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no es tuyo");
    }

    @Test
    void encontrarActividadPorIdMaestro_maestroPropietario_devuelveActividad() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        Actividad result = actividadService.encontrarActividadPorIdMaestro(5L);
        assertThat(result).isSameAs(actividad);
    }

    @Test
    void encontrarActividadPorIdAlumno_usuarioNoEsAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.encontrarActividadPorIdAlumno(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("No tienes permiso");
    }

    @Test
    void encontrarActividadPorIdAlumno_alumnoNoInscrito_lanzaAccessDeniedException() {
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
        assertThatThrownBy(() -> actividadService.encontrarActividadPorIdAlumno(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no estás inscrito");
    }

    @Test
    void encontrarActividadPorIdAlumno_cursoOculto_lanzaAccessDeniedException() {
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

        assertThatThrownBy(() -> actividadService.encontrarActividadPorIdAlumno(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("curso oculto");
    }

    @Test
    void encontrarActividadPorIdAlumno_alumnoInscrito_devuelveActividad() {
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
        Actividad result = actividadService.encontrarActividadPorIdAlumno(5L);
        assertThat(result).isSameAs(actividad);
    }

    @Test
    void deleteActividad_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.deleteActividad(1L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void deleteActividad_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.deleteActividad(5L))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no son tuyos");
    }

    @Test
    void deleteActividad_maestroPropietario_eliminaActividad() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        actividadService.deleteActividad(5L);
        verify(actividadRepository).delete(actividad);
    }

    @Test
    void updateActividadTeoria_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});
        assertThatThrownBy(() -> actividadService.updateActividadTeoria(1L, "T", "D", null))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("Solo un maestro");
    }

    @Test
    void updateActividadTeoria_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro maestro = crearMaestro(1L);
        Maestro otroMaestro = crearMaestro(2L);
        Curso curso = crearCurso(otroMaestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        assertThatThrownBy(() -> actividadService.updateActividadTeoria(5L, "T", "D", null))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            .hasMessageContaining("no son tuyos");
    }

    @Test
    void updateActividadTeoria_maestroPropietario_actualizaYGuarda() {
        Maestro maestro = crearMaestro(1L);
        Curso curso = crearCurso(maestro);
        Tema tema = crearTema(10L, curso);
        Actividad actividad = new General();
        actividad.setId(5L);
        actividad.setTema(tema);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(java.util.Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));
        Actividad result = actividadService.updateActividadTeoria(5L, "Nuevo T", "Nueva D", "img.png");
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