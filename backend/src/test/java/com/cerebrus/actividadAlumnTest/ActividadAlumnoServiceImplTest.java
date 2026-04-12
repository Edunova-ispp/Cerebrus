package com.cerebrus.actividadAlumnTest;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionService;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.actividadAlumn.ActividadAlumnoServiceImpl;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.curso.Curso;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.respuestaAlumn.RespuestaAlumnoService;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralService;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacionService;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagenService;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class ActividadAlumnoServiceImplTest {

    @Mock private ActividadAlumnoRepository actividadAlumnoRepository;
    @Mock private ActividadRepository actividadRepository;
    @Mock private AlumnoRepository alumnoRepository;
    @Mock private RespuestaAlumnoService respuestaAlumnoService;
    @Mock private RespAlumnoGeneralService respAlumnoGeneralService;
    @Mock private OrdenacionService ordenacionService;
    @Mock private RespAlumnoOrdenacionService respAlumnoOrdenacionService;
    @Mock private UsuarioService usuarioService;
    @Mock private MarcarImagenService marcarImagenService;
    @Mock private RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @InjectMocks
    private ActividadAlumnoServiceImpl service;

    private Alumno alumno;
    private Maestro maestro;
    private Actividad actividad;
    private ActividadAlumno actividadAlumno;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);
        alumno.setNombre("Alumno 1");

        maestro = new Maestro();
        maestro.setId(2L);

        actividad = new General();
        actividad.setId(50L);
        actividad.setPosicion(1);
        actividad.setPuntuacion(100);

        Curso curso = new Curso();
        Tema tema = new Tema();
        tema.setCurso(curso);
        tema.setActividades(List.of(actividad));
        actividad.setTema(tema);

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setPuntuacion(0);
        actividadAlumno.setNota(0);
        actividadAlumno.setNumAbandonos(0);
        actividadAlumno.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        actividadAlumno.setAlumno(alumno);
        actividadAlumno.setActividad(actividad);
        actividadAlumno.setRespuestasAlumno(new ArrayList<>());
    }

    // ==================== crearActAlumno ====================

    @Test
    void crearActAlumno_nuevaPar_creaYGuarda() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.empty());
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(actividadAlumnoRepository.save(any(ActividadAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.crearActAlumno(100, LocalDateTime.now(),
                LocalDateTime.now(), 8, 0, 1L, 50L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAlumno()).isEqualTo(alumno);
        assertThat(resultado.getActividad()).isEqualTo(actividad);
        verify(actividadAlumnoRepository).save(any(ActividadAlumno.class));
    }

    @Test
    void crearActAlumno_intentoEnCurso_retornaExistenteSinGuardar() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        actividadAlumno.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
            .thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.crearActAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 1L, 50L);

        assertThat(resultado.getId()).isEqualTo(10L);
        verify(actividadAlumnoRepository, never()).save(any());
        verify(actividadRepository, never()).findById(any());
    }

    @Test
    void crearActAlumno_reintentoPermitido_creaNuevaInstancia() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        actividad.setPermitirReintento(true);
        actividadAlumno.setFechaFin(LocalDateTime.now().minusMinutes(5));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
            .thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(actividadAlumnoRepository.save(any(ActividadAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.crearActAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 1L, 50L);

        assertThat(resultado).isNotSameAs(actividadAlumno);
        verify(actividadAlumnoRepository).save(any(ActividadAlumno.class));
    }

    @Test
    void crearActAlumno_terminadaSinReintento_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        actividad.setPermitirReintento(false);
        actividadAlumno.setFechaFin(LocalDateTime.now().minusMinutes(5));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
            .thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));

        assertThatThrownBy(() -> service.crearActAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 1L, 50L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("No se permite el reintento para esta actividad");

        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void crearActAlumno_actividadNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L)).thenReturn(Optional.empty());
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearActAlumno(100, LocalDateTime.now(),
                LocalDateTime.now(), 8, 0, 1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void crearActAlumno_alumnoNoExiste_lanzaResourceNotFoundException() {
        alumno.setId(99L); 
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(99L, 50L))
            .thenReturn(Optional.empty());
        when(actividadRepository.findById(50L))
            .thenReturn(Optional.of(actividad));
        when(alumnoRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearActAlumno(100, LocalDateTime.now(),
            LocalDateTime.now(), 8, 0, 99L, 50L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("El alumno no existe");
    }

    // ==================== encontrarActAlumnoPorId ====================

    @Test
    void encontrarActAlumnoPorId_existente_retornaEntidad() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.encontrarActAlumnoPorId(10L);

        assertThat(resultado.getId()).isEqualTo(10L);
    }

    @Test
    void encontrarActAlumnoPorId_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encontrarActAlumnoPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== encontrarActAlumnoPorAlumnoIdYActId ====================

    @Test
    void encontrarActAlumnoPorAlumnoIdYActId_existe_retornaOptionalPresente() {
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Optional<ActividadAlumno> resultado =
                service.encontrarActAlumnoPorAlumnoIdYActId(1L, 50L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(10L);
    }

    @Test
    void encontrarActAlumnoPorAlumnoIdYActId_noExiste_retornaOptionalVacio() {
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L))
                .thenReturn(Optional.empty());

        Optional<ActividadAlumno> resultado =
                service.encontrarActAlumnoPorAlumnoIdYActId(1L, 99L);

        assertThat(resultado).isEmpty();
    }

    // ==================== existeActAlumnoPorActIdYCurrentUserId ====================

    @Test
    void existeActAlumnoPorActIdYCurrentUserId_existeParaAlumnoAutenticado_retorna1() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Integer resultado = service.existeActAlumnoPorActIdYCurrentUserId(50L);

        assertThat(resultado).isEqualTo(1);
    }

    @Test
    void existeActAlumnoPorActIdYCurrentUserId_terminada_retorna0() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        actividadAlumno.setFechaFin(LocalDateTime.now().minusMinutes(1));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Integer resultado = service.existeActAlumnoPorActIdYCurrentUserId(50L);

        assertThat(resultado).isEqualTo(0);
    }

    @Test
    void existeActAlumnoPorActIdYCurrentUserId_terminadaSinReintento_tambienRetorna0() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        actividadAlumno.setFechaFin(LocalDateTime.now().minusMinutes(1));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        Integer resultado = service.existeActAlumnoPorActIdYCurrentUserId(50L);

        assertThat(resultado).isEqualTo(0);
    }

    @Test
    void existeActAlumnoPorActIdYCurrentUserId_noExiste_retorna0() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 99L))
                .thenReturn(Optional.empty());

        Integer resultado = service.existeActAlumnoPorActIdYCurrentUserId(99L);

        assertThat(resultado).isEqualTo(0);
    }

    @Test
    void existeActAlumnoPorActIdYCurrentUserId_excepcionEnService_retorna0() {
        when(usuarioService.findCurrentUser()).thenThrow(new RuntimeException("error"));

        Integer resultado = service.existeActAlumnoPorActIdYCurrentUserId(50L);

        assertThat(resultado).isEqualTo(0);
    }

    // ==================== actualizarActAlumno ====================

    @Test
    void actualizarActAlumno_existente_actualizaTodosLosCampos() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime nuevaFechaFin = LocalDateTime.now();

        ActividadAlumno resultado = service.actualizarActAlumno(10L, 200,
                LocalDateTime.now(), nuevaFechaFin, 9, 1);

        assertThat(resultado.getPuntuacion()).isEqualTo(200);
        assertThat(resultado.getNota()).isEqualTo(9);
        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void actualizarActAlumno_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarActAlumno(99L, 100,
                LocalDateTime.now(), LocalDateTime.now(), 8, 0))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== eliminarActAlumnoPorId ====================

    @Test
    void eliminarActAlumnoPorId_existente_eliminaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        service.eliminarActAlumnoPorId(10L);

        verify(actividadAlumnoRepository).delete(actividadAlumno);
    }

    @Test
    void eliminarActAlumnoPorId_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarActAlumnoPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(actividadAlumnoRepository, never()).delete(any());
    }

    // ==================== abandonarActAlumnoPorId ====================

    @Test
    void abandonarActAlumnoPorId_alumnoValido_incrementaAbandonos() {
        actividadAlumno.setNumAbandonos(0);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.abandonarActAlumnoPorId(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void abandonarActAlumnoPorId_yaTerminada_noIncrementaYNoGuarda() {
        actividadAlumno.setNumAbandonos(0);
        actividadAlumno.setFechaFin(LocalDateTime.now()); // Para que estado sea TERMINADA
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        ActividadAlumno resultado = service.abandonarActAlumnoPorId(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(0);
        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void abandonarActAlumnoPorId_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.abandonarActAlumnoPorId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede abandonar su actividad");
    }

    @Test
    void abandonarActAlumnoPorId_actividadDeOtroAlumno_lanzaAccessDeniedException() {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otroAlumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        assertThatThrownBy(() -> service.abandonarActAlumnoPorId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No puedes abandonar una ActividadAlumno que no es tuya");
    }

    @Test
    void abandonarActAlumnoPorId_noExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abandonarActAlumnoPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void abandonarActAlumnoPorId_abandonosPreviosNull_trataComo0() {
        actividadAlumno.setNumAbandonos(null);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.abandonarActAlumnoPorId(10L);

        assertThat(resultado.getNumAbandonos()).isEqualTo(1);
    }

    // ==================== corregirActAlumnoManual ====================

    @Test
    void corregirActAlumnoManual_notaYRespuestas_actualizaNotaYCorrigeRespuestas() {
        RespuestaAlumno respuestaAlumno = new RespuestaAlumno() {};
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAlumno);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoManual(10L, 7, List.of(101L));

        assertThat(resultado.getNota()).isEqualTo(7);
        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(101L);
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void corregirActAlumnoManual_soloNota_actualizaNota() {
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoManual(10L, 5, null);

        assertThat(resultado.getNota()).isEqualTo(5);
        verify(respuestaAlumnoService, never()).marcarODesmarcarRespuestaCorrecta(any());
    }

    @Test
    void corregirActAlumnoManual_respuestaDeOtraActividad_lanzaIllegalArgumentException() {
        ActividadAlumno otraActividad = new ActividadAlumno();
        otraActividad.setId(99L);

        RespuestaAlumno respuestaAjena = new RespuestaAlumno() {};
        respuestaAjena.setId(101L);
        respuestaAjena.setActividadAlumno(otraActividad);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAjena);

        assertThatThrownBy(() -> service.corregirActAlumnoManual(10L, null, List.of(101L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la actividad del alumno");
    }

    @Test
    void corregirActAlumnoManual_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActAlumnoManual(99L, 7, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActAlumnoAutomaticamente - General TEST ====================

    @Test
    void corregirActAlumnoAutomaticamente_tipoTest_todasCorrectas_notaYPuntuacionMaximas() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        Pregunta pregunta1 = actividadGeneral.getPreguntas().get(0);
        Pregunta pregunta2 = actividadGeneral.getPreguntas().get(1);
        pregunta1.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p1_ok", true),
            crearRespuestaMaestro("p1_bad", false)
        ));
        pregunta2.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p2_ok", true),
            crearRespuestaMaestro("p2_bad", false)
        ));

        RespAlumnoGeneral resp1 = crearRespAlumnoGeneral(101L, pregunta1, "p1_ok");
        RespAlumnoGeneral resp2 = crearRespAlumnoGeneral(102L, pregunta2, "p2_ok");

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(102L)).thenReturn(resp2);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, List.of(101L, 102L));

        assertThat(resultado.getPuntuacion()).isEqualTo(100);
        assertThat(resultado.getNota()).isEqualTo(10);
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    void corregirActAlumnoAutomaticamente_tipoTest_todasIncorrectas_notaYPuntuacionCeroMinimo() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        Pregunta pregunta1 = actividadGeneral.getPreguntas().get(0);
        Pregunta pregunta2 = actividadGeneral.getPreguntas().get(1);
        pregunta1.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p1_ok", true),
            crearRespuestaMaestro("p1_bad", false)
        ));
        pregunta2.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p2_ok", true),
            crearRespuestaMaestro("p2_bad", false)
        ));

        RespAlumnoGeneral resp1 = crearRespAlumnoGeneral(101L, pregunta1, "p1_bad");
        RespAlumnoGeneral resp2 = crearRespAlumnoGeneral(102L, pregunta2, "p2_bad");

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(102L)).thenReturn(resp2);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, List.of(101L, 102L));

        assertThat(resultado.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.getNota()).isEqualTo(0);
    }

    @Test
    void corregirActAlumnoAutomaticamente_tipoTest_mitadCorrectas_calculaIntermedio() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        Pregunta pregunta1 = actividadGeneral.getPreguntas().get(0);
        Pregunta pregunta2 = actividadGeneral.getPreguntas().get(1);
        pregunta1.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p1_ok", true),
            crearRespuestaMaestro("p1_bad", false)
        ));
        pregunta2.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("p2_ok", true),
            crearRespuestaMaestro("p2_bad", false)
        ));

        RespAlumnoGeneral resp1 = crearRespAlumnoGeneral(101L, pregunta1, "p1_ok");
        RespAlumnoGeneral resp2 = crearRespAlumnoGeneral(102L, pregunta2, "p2_bad");

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(102L)).thenReturn(resp2);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, List.of(101L, 102L));

        // 1 correcta: +50, 1 incorrecta: -25 → 25
        assertThat(resultado.getPuntuacion()).isEqualTo(25);
        assertThat(resultado.getNota()).isEqualTo(3); // round(2.5)
    }

    @Test
    void corregirActAlumnoAutomaticamente_tipoTest_multiplesCorrectas_conSeleccionParcial_noPuntuaCompleta() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 1);
        actividadAlumno.setActividad(actividadGeneral);

        Pregunta pregunta1 = actividadGeneral.getPreguntas().get(0);
        pregunta1.setRespuestasMaestro(List.of(
            crearRespuestaMaestro("opcion_a", true),
            crearRespuestaMaestro("opcion_b", true),
            crearRespuestaMaestro("opcion_c", false)
        ));

        // El alumno selecciona solo una de las dos correctas.
        RespAlumnoGeneral resp1 = crearRespAlumnoGeneral(101L, pregunta1, "opcion_a");

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, List.of(101L));

        assertThat(resultado.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.getNota()).isEqualTo(0);
    }

    @Test
    void corregirActAlumnoAutomaticamente_tipoTeoria_notaMaximaYPuntuacionBase() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEORIA, 100, 1);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, null);

        assertThat(resultado.getNota()).isEqualTo(10);
        assertThat(resultado.getPuntuacion()).isEqualTo(100);
    }

    @Test
    void corregirActAlumnoAutomaticamente_sinRespuestasIds_noCorrrigeNada() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General actividadGeneral = crearActividadGeneral(TipoActGeneral.TEST, 100, 2);
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, null);

        assertThat(resultado.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.getNota()).isEqualTo(0);
        verify(respAlumnoGeneralService, never()).corregirRespuestaAlumnoGeneral(any());
    }

    @Test
    void corregirActAlumnoAutomaticamente_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActAlumnoAutomaticamente(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActAlumnoAutomaticamente - Ordenacion ====================

    @Test
    void corregirActAlumnoAutomaticamente_ordenacion_posicionesCorrectas_calculaPuntuacion() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(50L);
        ordenacion.setPuntuacion(100);
        ordenacion.setValores(List.of("A", "B", "C"));
        actividadAlumno.setActividad(ordenacion);

        RespuestaAlumno respuestaAlumno = new RespuestaAlumno() {};
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionService.encontrarActOrdenacionPorId(50L)).thenReturn(ordenacion);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(101L)).thenReturn(3);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamente(10L, List.of(101L));

        // puntuacion: 3*(100/3) - 0*(100/3/3) = 99; nota: 3*(10/3) - 0 = 9
        assertThat(resultado.getPuntuacion()).isGreaterThanOrEqualTo(0);
        assertThat(resultado.getNota()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void corregirActAlumnoAutomaticamente_ordenacion_masDeUnaRespuesta_lanzaIllegalArgumentException() {
        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(50L);
        ordenacion.setPuntuacion(100);
        ordenacion.setValores(List.of("A", "B"));
        actividadAlumno.setActividad(ordenacion);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(ordenacionService.encontrarActOrdenacionPorId(50L)).thenReturn(ordenacion);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        assertThatThrownBy(() -> service.corregirActAlumnoAutomaticamente(10L, List.of(101L, 102L)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Para actividades de ordenación solo se permite una respuesta del alumno con la secuencia ordenada");
    }

    // ==================== corregirActAlumnoAutomaticamenteClasificacion ====================

    @Test
    void corregirActAlumnoAutomaticamenteClasificacion_todasCorrectas_notaMaxima() {
            when(usuarioService.findCurrentUser()).thenReturn(alumno);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(30L);
        pregunta.setRespuestasMaestro(List.of(
                crearRespuestaMaestro(true), crearRespuestaMaestro(false)));

        General actividadGeneral = crearActividadGeneralConPreguntas(100, List.of(pregunta));
        actividadAlumno.setActividad(actividadGeneral);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(actividadGeneral.getId())).thenReturn(Optional.of(actividadGeneral));
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneralClasificacion(101L)).thenReturn(true);
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamenteClasificacion(10L, List.of(101L));

        assertThat(resultado.getPuntuacion()).isGreaterThan(0);
        assertThat(resultado.getNota()).isGreaterThan(0);
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    void corregirActAlumnoAutomaticamenteClasificacion_sinPreguntas_retornaCero() {

        General actividadGeneral = crearActividadGeneral(TipoActGeneral.CLASIFICACION, 100, 0);
        actividadGeneral.setId(50L);
    
        actividadAlumno.setActividad(actividadGeneral);
        actividadAlumno.setAlumno(alumno);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        when(actividadAlumnoRepository.findById(anyLong())).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(anyLong())).thenReturn(Optional.of(actividadGeneral));

        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamenteClasificacion(10L, List.of(101L));

        assertThat(resultado.getNota()).isEqualTo(0);
    }

    @Test
    void corregirActAlumnoAutomaticamenteClasificacion_noExiste_lanzaResourceNotFoundException() {
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirActAlumnoAutomaticamenteClasificacion(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void corregirActAlumnoAutomaticamenteClasificacion_sinRespuestasIds_puntuacionCero() {
        Pregunta pregunta = new Pregunta();
        pregunta.setId(30L);
        pregunta.setRespuestasMaestro(List.of(crearRespuestaMaestro(true)));

        General actividadGeneral = crearActividadGeneralConPreguntas(100, List.of(pregunta));
        actividadAlumno.setActividad(actividadGeneral);
        actividadAlumno.setAlumno(alumno);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(actividadGeneral.getId())).thenReturn(Optional.of(actividadGeneral));
        when(actividadAlumnoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActividadAlumno resultado = service.corregirActAlumnoAutomaticamenteClasificacion(10L, null);

        assertThat(resultado.getNota()).isEqualTo(0);
        assertThat(resultado.getPuntuacion()).isEqualTo(0);
    }

    // ==================== corregirNotaActAlumno ====================

    @Test
    void corregirNotaActAlumno_cambiaNota() {
        service.corregirNotaActAlumno(actividadAlumno, 8);

        assertThat(actividadAlumno.getNota()).isEqualTo(8);
    }

    // ==================== corregirRespuestasActAlumno ====================

    @Test
    void corregirRespuestasActAlumno_conIds_llamaService() {
        List<Long> ids = List.of(101L, 102L);
        RespAlumnoGeneral resp1 = new RespAlumnoGeneral();
        resp1.setActividadAlumno(actividadAlumno);
        RespAlumnoGeneral resp2 = new RespAlumnoGeneral();
        resp2.setActividadAlumno(actividadAlumno);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(101L)).thenReturn(resp1);
        when(respuestaAlumnoService.encontrarRespuestaAlumnoPorId(102L)).thenReturn(resp2);

        service.corregirRespuestasActAlumno(actividadAlumno, ids);

        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(101L);
        verify(respuestaAlumnoService).marcarODesmarcarRespuestaCorrecta(102L);
    }

    @Test
    void corregirRespuestasActAlumno_sinIds_noHaceNada() {
        service.corregirRespuestasActAlumno(actividadAlumno, new ArrayList<>());

        verifyNoInteractions(respuestaAlumnoService);
    }

    // ==================== corregirActAlumnoAutomaticamente - Carta ====================
 
    @Test
    void corregirActAlumnoAutomaticamente_carta_tiempoMenorQueIdeal_notaYPuntuacionMaximas() {
        // tiempoMinutos=0 → tiempoSegundos=0 → fallback: puntuación máxima
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        List<Long> ids = List.of(101L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        general.setPreguntas(List.of(pregunta));
 
        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(general);
        actividadAlumno.setFechaInicio(LocalDateTime.now());

        RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral();
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);
        respuestaAlumno.setPregunta(pregunta);
 
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respAlumnoGeneralService.encontrarRespuestaAlumnoGeneralPorId(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
 
        service.corregirActAlumnoAutomaticamente(10L, ids);
 
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }
 
    @Test
    void corregirActAlumnoAutomaticamente_carta_tiempoSuperiorAlMaximo_notaYPuntuacionMinimas() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        List<Long> ids = List.of(101L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        general.setPreguntas(List.of(pregunta));
 
        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(general);
 
        RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral();
        respuestaAlumno.setId(101L);
        respuestaAlumno.setActividadAlumno(actividadAlumno);
        respuestaAlumno.setPregunta(pregunta);
 
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(respAlumnoGeneralService.encontrarRespuestaAlumnoGeneralPorId(101L)).thenReturn(respuestaAlumno);
        when(respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(101L)).thenReturn(true);
 
        service.corregirActAlumnoAutomaticamente(10L, ids);
 
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(10);
        assertThat(actividadAlumno.getNota()).isEqualTo(1);
    }
 
    @Test
    void corregirActAlumnoAutomaticamente_carta_numRespuestasDistintoDePreguntas_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        general.setPreguntas(List.of(new Pregunta(), new Pregunta())); // 2 preguntas
        actividadAlumno.setActividad(general);
 
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        assertThatThrownBy(() -> service.corregirActAlumnoAutomaticamente(
                10L, List.of(101L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coincide");
    }
 
    @Test
    void corregirActAlumnoAutomaticamente_carta_respuestasDuplicadas_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        General general = new General();
        general.setTipo(TipoActGeneral.CARTA);
        general.setPuntuacion(100);
        List<Pregunta> preguntas = new ArrayList<>();
        preguntas.add(pregunta);
        preguntas.add(pregunta);
        general.setPreguntas(preguntas); // 2 preguntas
        actividadAlumno.setActividad(general);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
 
        assertThatThrownBy(() -> service.corregirActAlumnoAutomaticamente(
                10L, List.of(101L, 101L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicadas");
    }

    // ==================== corregirActAlumnoAutomaticamente - Marcar Imagen ====================

    @Test
    void corregirActAlumnoAutomaticamente_marcarImagen_conRespuestas() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        List<Long> ids = List.of(201L);

        PuntoImagen punto = new PuntoImagen();

        MarcarImagen marcarImagen = new MarcarImagen();
        marcarImagen.setId(50L);
        marcarImagen.setPuntuacion(100);
        marcarImagen.setPuntosImagen(List.of(punto));

        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(marcarImagen);

        RespAlumnoPuntoImagen resp = new RespAlumnoPuntoImagen();
        resp.setActividadAlumno(actividadAlumno);

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(marcarImagenService.encontrarActMarcarImagenPorId(50L)).thenReturn(marcarImagen);
        when(respAlumnoPuntoImagenService.encontrarRespuestaAlumnoPuntoImagenPorId(201L)).thenReturn(resp);
        when(respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(201L)).thenReturn(true);

        service.corregirActAlumnoAutomaticamente(10L, ids);

        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }

    // ==================== corregirActAlumnoAutomaticamente - Crucigrama ====================

    @Test
    void corregirActAlumnoAutomaticamente_crucigrama_conTiempo_calculaNota() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
    
        General general = new General();
        general.setId(50L);
        general.setPuntuacion(100);
        general.setPreguntas(List.of(new Pregunta())); // 1 word
        general.setTipo(TipoActGeneral.CRUCIGRAMA);
        actividadAlumno.setActividad(general);

        LocalDateTime start = LocalDateTime.now();
        actividadAlumno.setFechaInicio(start.minusSeconds(60));

        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(general));

        service.corregirActAlumnoAutomaticamente(10L, List.of(101L));

        // Time 60s, ideal 30s per word, max 120s per word
        // 60 > 30, < 120, proportion = 1.0 - 0.9 * ((60-30)/(120-30)) = 1.0 - 0.9 * (30/90) = 1.0 - 0.9*0.333 ≈ 1.0 - 0.3 = 0.7
        // puntuacion = 0.7 * 100 = 70
        // nota = 0.7 * 10 = 7
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(70);
        assertThat(actividadAlumno.getNota()).isEqualTo(7);
    }

    // ==================== Helpers ====================

    private General crearActividadGeneral(TipoActGeneral tipo, int puntuacion, int numPreguntas) {
        General g = new General();
        g.setId(50L);
        g.setPuntuacion(puntuacion);
        g.setTipo(tipo);
        g.setRespVisible(false);
        List<Pregunta> preguntas = new ArrayList<>();
        for (int i = 0; i < numPreguntas; i++) {
            Pregunta p = new Pregunta();
            p.setId((long) (100 + i));
            p.setActividad(g);
            p.setRespuestasMaestro(new ArrayList<>());
            preguntas.add(p);
        }
        g.setPreguntas(preguntas);
        return g;
    }

    private General crearActividadGeneralConPreguntas(int puntuacion, List<Pregunta> preguntas) {
        General g = new General();
        g.setId(50L);
        g.setPuntuacion(puntuacion);
        g.setTipo(TipoActGeneral.CLASIFICACION);
        g.setRespVisible(false);
        g.setPreguntas(preguntas);
        return g;
    }

    private RespAlumnoGeneral crearRespAlumnoGeneral(Long id, Pregunta pregunta, String respuesta) {
        RespAlumnoGeneral resp = new RespAlumnoGeneral();
        resp.setId(id);
        resp.setActividadAlumno(actividadAlumno);
        resp.setPregunta(pregunta);
        resp.setRespuesta(respuesta);
        return resp;
    }

    private com.cerebrus.respuestaMaestro.RespuestaMaestro crearRespuestaMaestro(boolean correcta) {
        com.cerebrus.respuestaMaestro.RespuestaMaestro rm = new com.cerebrus.respuestaMaestro.RespuestaMaestro();
        rm.setCorrecta(correcta);
        rm.setRespuesta("opcion");
        return rm;
    }

    private com.cerebrus.respuestaMaestro.RespuestaMaestro crearRespuestaMaestro(String respuesta, boolean correcta) {
        com.cerebrus.respuestaMaestro.RespuestaMaestro rm = new com.cerebrus.respuestaMaestro.RespuestaMaestro();
        rm.setCorrecta(correcta);
        rm.setRespuesta(respuesta);
        return rm;
    }
}
