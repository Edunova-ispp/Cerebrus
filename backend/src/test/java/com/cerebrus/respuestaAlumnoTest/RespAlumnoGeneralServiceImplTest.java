package com.cerebrus.respuestaAlumnoTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaRepository;
import com.cerebrus.respuestaalumno.RespAlumnoGeneral;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralCreateResponse;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralRepository;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralServiceImpl;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class RespAlumnoGeneralServiceImplTest {

    @Mock
    private RespAlumnoGeneralRepository respAlumnoGeneralRepository;

    @Mock
    private ActividadAlumnoRepository actividadAlumnoRepository;

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private RespAlumnoGeneralServiceImpl service;

    // Para capturar el objeto RespAlumnoGeneral que se guarda en el repositorio
    @Captor
    private ArgumentCaptor<RespAlumnoGeneral> respAlumnoGeneralCaptor;

    // Test para verificar que el método crearRespAlumnoGeneral lanza AccessDeniedException cuando el usuario 
    // actual no es un alumno, y no interactúa con los repositorios
    @Test
    void crearRespAlumnoGeneral_cuandoUsuarioNoEsAlumno_lanzaAccessDenied_yNoTocaRepos() {
        when(usuarioService.findCurrentUser()).thenReturn(new Maestro());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "A", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un alumno");

        verify(usuarioService).findCurrentUser();
        verifyNoInteractions(actividadAlumnoRepository, preguntaRepository, respuestaRepository, respAlumnoGeneralRepository);
    }

    // Test para verificar que el método crearRespAlumnoGeneral lanza AccessDeniedException cuando no hay usuario 
    // autenticado, y no interactúa con los repositorios
    @Test
    void crearRespAlumnoGeneral_cuandoUsuarioEsNull_lanzaAccessDenied_yNoTocaRepos() {
        when(usuarioService.findCurrentUser()).thenReturn(null);

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "A", 2L))
                .isInstanceOf(AccessDeniedException.class);

        verify(usuarioService).findCurrentUser();
        verifyNoInteractions(actividadAlumnoRepository, preguntaRepository, respuestaRepository, respAlumnoGeneralRepository);
    }

    // Test para verificar que el método crearRespAlumnoGeneral funciona correctamente cuando respVisible es true, 
    // devuelve el comentario de la actividad, y guarda la entidad con los campos correctos
    @Test
    void crearRespAlumnoGeneral_ok_respVisibleTrue_devuelveComentarioDeActividad_yGuardaEntidad() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        ActividadAlumno actAlumno = new ActividadAlumno();
        actAlumno.setId(10L);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actAlumno));

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        when(actividad.getRespVisible()).thenReturn(true);
        when(actividad.getComentariosRespVisible()).thenReturn("Buen trabajo");

        Pregunta pregunta = new Pregunta();
        pregunta.setId(20L);
        pregunta.setActividad(actividad);
        when(preguntaRepository.findById(20L)).thenReturn(Optional.of(pregunta));

        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setId(30L);
        respuestaObj.setCorrecta(true);
        when(respuestaRepository.findByRespuesta("RESP")).thenReturn(Optional.of(respuestaObj));

        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class))).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneralCreateResponse response = service.crearRespAlumnoGeneral(10L, "RESP", 20L);

        assertThat(response).isNotNull();
        assertThat(response.getComentario()).isEqualTo("Buen trabajo");
        assertThat(response.getRespAlumnoGeneral()).isNotNull();
        assertThat(response.getRespAlumnoGeneral().getCorrecta()).isTrue();
        assertThat(response.getRespAlumnoGeneral().getRespuesta()).isEqualTo("RESP");
        assertThat(response.getRespAlumnoGeneral().getActividadAlumno()).isSameAs(actAlumno);
        assertThat(response.getRespAlumnoGeneral().getPregunta()).isSameAs(pregunta);

        verify(respAlumnoGeneralRepository).save(respAlumnoGeneralCaptor.capture());
        RespAlumnoGeneral saved = respAlumnoGeneralCaptor.getValue();
        assertThat(saved.getCorrecta()).isTrue();
        assertThat(saved.getActividadAlumno()).isSameAs(actAlumno);
        assertThat(saved.getRespuesta()).isEqualTo("RESP");
        assertThat(saved.getPregunta()).isSameAs(pregunta);
    }

    // Test para verificar que el método crearRespAlumnoGeneral funciona correctamente cuando respVisible es false, 
    // devuelve un comentario vacío, y guarda la entidad con los campos correctos
    @Test
    void crearRespAlumnoGeneral_ok_respVisibleFalse_devuelveComentarioVacio() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        ActividadAlumno actAlumno = new ActividadAlumno();
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(actAlumno));

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        when(actividad.getRespVisible()).thenReturn(false);

        Pregunta pregunta = new Pregunta();
        pregunta.setActividad(actividad);
        when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));

        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setCorrecta(false);
        when(respuestaRepository.findByRespuesta("X")).thenReturn(Optional.of(respuestaObj));

        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class))).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneralCreateResponse response = service.crearRespAlumnoGeneral(1L, "X", 2L);

        assertThat(response.getComentario()).isEqualTo("");
        assertThat(response.getRespAlumnoGeneral().getCorrecta()).isFalse();
        verify(actividad, never()).getComentariosRespVisible();
    }

    // Test para verificar que el método crearRespAlumnoGeneral permite que el comentario de la actividad sea null 
    // cuando respVisible es true, y guarda la entidad con comentariosRespVisible null
    @Test
    void crearRespAlumnoGeneral_ok_respVisibleTrue_permiteComentarioNull() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(new ActividadAlumno()));

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        when(actividad.getRespVisible()).thenReturn(true);
        when(actividad.getComentariosRespVisible()).thenReturn(null);

        Pregunta pregunta = new Pregunta();
        pregunta.setActividad(actividad);
        when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));

        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setCorrecta(true);
        when(respuestaRepository.findByRespuesta("X")).thenReturn(Optional.of(respuestaObj));

        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class))).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneralCreateResponse response = service.crearRespAlumnoGeneral(1L, "X", 2L);

        assertThat(response.getComentario()).isNull();
    }

    // Test para verificar que el método crearRespAlumnoGeneral lanza NullPointerException cuando respVisible es null,
    // y no interactúa con el repositorio de respuestas del alumno
    @Test
    void crearRespAlumnoGeneral_respVisibleNull_lanzaNullPointer() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(new ActividadAlumno()));

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        when(actividad.getRespVisible()).thenReturn(null);

        Pregunta pregunta = new Pregunta();
        pregunta.setActividad(actividad);
        when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));

        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setCorrecta(true);
        when(respuestaRepository.findByRespuesta("X")).thenReturn(Optional.of(respuestaObj));

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "X", 2L))
                .isInstanceOf(NullPointerException.class);

        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    // Test para verificar que el método crearRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // actividad del alumno no existe, y no interactúa con los repositorios de preguntas ni respuestas 
    // ni de respuestas del alumno
    @Test
    void crearRespAlumnoGeneral_cuandoActividadAlumnoNoExiste_lanzaRuntimeConMensaje() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "X", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad del alumno no existe");
    }

    // Test para verificar que el método crearRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // pregunta no existe, y no interactúa con el repositorio de respuestas del alumno ni con el repositorio de respuestas
    @Test
    void crearRespAlumnoGeneral_cuandoPreguntaNoExiste_lanzaRuntimeConMensaje() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(new ActividadAlumno()));
        when(preguntaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "X", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La pregunta no existe");
    }

    // Test para verificar que el método crearRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // respuesta no existe, y no interactúa con el repositorio de respuestas del alumno
    @Test
    void crearRespAlumnoGeneral_cuandoRespuestaNoExiste_lanzaRuntimeConMensaje() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(new ActividadAlumno()));
        Pregunta pregunta = new Pregunta();
        when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));

        when(respuestaRepository.findByRespuesta("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(1L, "X", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta no existe");
    }

    // Test para verificar que el método crearRespAlumnoGeneral guarda null en el campo correcta cuando la 
    // respuesta encontrada tiene correcta null, y que se guarda la entidad con los campos correctos
    @Test
    void crearRespAlumnoGeneral_respuestaCorrectaNull_seGuardaNull() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        ActividadAlumno actAlumno = new ActividadAlumno();
        when(actividadAlumnoRepository.findById(1L)).thenReturn(Optional.of(actAlumno));

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        when(actividad.getRespVisible()).thenReturn(false);
        Pregunta pregunta = new Pregunta();
        pregunta.setActividad(actividad);
        when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));

        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setCorrecta(null);
        when(respuestaRepository.findByRespuesta("X")).thenReturn(Optional.of(respuestaObj));

        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class))).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneralCreateResponse response = service.crearRespAlumnoGeneral(1L, "X", 2L);

        assertThat(response.getRespAlumnoGeneral().getCorrecta()).isNull();
        verify(respAlumnoGeneralRepository).save(respAlumnoGeneralCaptor.capture());
        assertThat(respAlumnoGeneralCaptor.getValue().getCorrecta()).isNull();
    }

    // Test para verificar que el método readRespAlumnoGeneral devuelve la entidad encontrada por el repositorio, 
    // y no interactúa con el servicio de usuarios
    @Test
    void readRespAlumnoGeneral_ok_devuelveEntidad() {
        RespAlumnoGeneral existing = new RespAlumnoGeneral();
        existing.setId(5L);
        when(respAlumnoGeneralRepository.findById(5L)).thenReturn(Optional.of(existing));

        RespAlumnoGeneral result = service.readRespAlumnoGeneral(5L);

        assertThat(result).isSameAs(existing);
        verify(respAlumnoGeneralRepository).findById(5L);
        verifyNoInteractions(usuarioService);
    }

    // Test para verificar que el método readRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el servicio de usuarios
    @Test
    void readRespAlumnoGeneral_cuandoNoExiste_lanzaRuntimeConMensaje() {
        when(respAlumnoGeneralRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readRespAlumnoGeneral(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");
        verifyNoInteractions(usuarioService);
    }

    // Test para verificar que el método updateRespAlumnoGeneral actualiza los campos correcta, respuesta y pregunta,
    // guarda la entidad, y no interactúa con los repositorios de respuestas del alumno ni de respuestas 
    // ni con el servicio de usuarios
    @Test
    void updateRespAlumnoGeneral_ok_actualizaCampos_yNoUsaActividadAlumnoNiRespuestaRepoNiUsuarioService() {
        RespAlumnoGeneral existing = new RespAlumnoGeneral();
        existing.setId(9L);
        existing.setCorrecta(false);
        existing.setRespuesta("ANTES");

        Actividad actividad = org.mockito.Mockito.mock(Actividad.class);
        Pregunta nuevaPregunta = new Pregunta();
        nuevaPregunta.setId(77L);
        nuevaPregunta.setActividad(actividad);

        when(respAlumnoGeneralRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(preguntaRepository.findById(77L)).thenReturn(Optional.of(nuevaPregunta));
        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class))).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneral updated = service.updateRespAlumnoGeneral(9L, true, 123L, "DESPUES", 77L);

        assertThat(updated).isSameAs(existing);
        assertThat(updated.getCorrecta()).isTrue();
        assertThat(updated.getRespuesta()).isEqualTo("DESPUES");
        assertThat(updated.getPregunta()).isSameAs(nuevaPregunta);

        verify(respAlumnoGeneralRepository).findById(9L);
        verify(preguntaRepository).findById(77L);
        verify(respAlumnoGeneralRepository).save(existing);
        verifyNoInteractions(actividadAlumnoRepository, respuestaRepository, usuarioService);
    }

    // Test para verificar que el método updateRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el repositorio de preguntas ni con el servicio de usuarios
    @Test
    void updateRespAlumnoGeneral_cuandoRespAlumnoNoExiste_lanzaRuntimeConMensaje() {
        when(respAlumnoGeneralRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRespAlumnoGeneral(9L, true, 1L, "X", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");
        verify(respAlumnoGeneralRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    // Test para verificar que el método updateRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // pregunta no existe, y no interactúa con el repositorio de respuestas del alumno ni con el servicio de usuarios
    @Test
    void updateRespAlumnoGeneral_cuandoPreguntaNoExiste_lanzaRuntimeConMensaje() {
        when(respAlumnoGeneralRepository.findById(9L)).thenReturn(Optional.of(new RespAlumnoGeneral()));
        when(preguntaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRespAlumnoGeneral(9L, true, 1L, "X", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La pregunta no existe");
        verify(respAlumnoGeneralRepository, never()).save(any());
        verifyNoInteractions(usuarioService);
    }

    // Test para verificar que el método deleteRespAlumnoGeneral borra la entidad encontrada por el repositorio, 
    // y no interactúa con el servicio de usuarios
    @Test
    void deleteRespAlumnoGeneral_ok_borraEntidad() {
        RespAlumnoGeneral existing = new RespAlumnoGeneral();
        existing.setId(3L);
        when(respAlumnoGeneralRepository.findById(3L)).thenReturn(Optional.of(existing));
        doNothing().when(respAlumnoGeneralRepository).delete(existing);

        service.deleteRespAlumnoGeneral(3L);

        verify(respAlumnoGeneralRepository).findById(3L);
        verify(respAlumnoGeneralRepository).delete(existing);
        verifyNoInteractions(usuarioService);
    }

    // Test para verificar que el método deleteRespAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el servicio de usuarios
    @Test
    void deleteRespAlumnoGeneral_cuandoNoExiste_lanzaRuntimeConMensaje() {
        when(respAlumnoGeneralRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRespAlumnoGeneral(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");
        verify(respAlumnoGeneralRepository, never()).delete(any());
        verifyNoInteractions(usuarioService);
    }
}
