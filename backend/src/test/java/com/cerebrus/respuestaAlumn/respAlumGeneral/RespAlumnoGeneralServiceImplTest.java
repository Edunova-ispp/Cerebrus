package com.cerebrus.respuestaAlumn.respAlumGeneral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.iaconnection.IaConnectionService;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaRequest;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.respuestaMaestro.RespuestaMaestroService;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class RespAlumnoGeneralServiceImplTest {

    @Mock private RespAlumnoGeneralRepository respAlumnoGeneralRepository;
    @Mock private ActividadAlumnoRepository actividadAlumnoRepository;
    @Mock private PreguntaRepository preguntaRepository;
    @Mock private RespuestaMaestroRepository respuestaRepository;
    @Mock private RespuestaMaestroService respuestaService;
    @Mock private UsuarioService usuarioService;
    @Mock private ActividadRepository actividadRepository;
    @Mock private IaConnectionService iaConnectionService;

    @InjectMocks
    private RespAlumnoGeneralServiceImpl service;

    private Alumno alumno;
    private Maestro maestro;
    private ActividadAlumno actividadAlumno;
    private General actividad;
    private Pregunta pregunta;
    private RespuestaMaestro respuestaCorrecta;
    private RespuestaMaestro respuestaIncorrecta;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);

        maestro = new Maestro();
        maestro.setId(2L);

        actividad = new General();
        actividad.setId(50L);
        actividad.setPuntuacion(100);
        actividad.setRespVisible(false);
        actividad.setComentariosRespVisible("Muy bien");

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setActividad(actividad);
        // numFallos is calculated from respuestasAlumno list, not set directly
        actividadAlumno.setNumAbandonos(0);

        pregunta = new Pregunta();
        pregunta.setId(30L);
        pregunta.setActividad(actividad);

        respuestaCorrecta = new RespuestaMaestro();
        respuestaCorrecta.setId(40L);
        respuestaCorrecta.setRespuesta("París");
        respuestaCorrecta.setCorrecta(true);

        respuestaIncorrecta = new RespuestaMaestro();
        respuestaIncorrecta.setId(41L);
        respuestaIncorrecta.setRespuesta("Madrid");
        respuestaIncorrecta.setCorrecta(false);
    }

    // ==================== crearRespAlumnoGeneral ====================

    @Test
    void crearRespAlumnoGeneral_respuestaCorrecta_retornaResponseConCorrectaTrue() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaRepository.findById(40L)).thenReturn(Optional.of(respuestaCorrecta));
        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class)))
                .thenAnswer(inv -> {
                    RespAlumnoGeneral r = inv.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        RespAlumnoGeneralCreateResponse resultado = service.crearRespAlumnoGeneral(10L, 40L, 30L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getCorrecta()).isTrue();
        verify(respAlumnoGeneralRepository).save(any(RespAlumnoGeneral.class));
    }

    @Test
    void crearRespAlumnoGeneral_respuestaIncorrecta_retornaResponseConCorrectaFalse() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaRepository.findById(41L)).thenReturn(Optional.of(respuestaIncorrecta));
        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class)))
                .thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(101L); return r; });

        RespAlumnoGeneralCreateResponse resultado = service.crearRespAlumnoGeneral(10L, 41L, 30L);

        assertThat(resultado.getCorrecta()).isFalse();
    }

    @Test
    void crearRespAlumnoGeneral_respVisibleTrue_devuelveComentario() {
        actividad.setRespVisible(true);
        actividad.setComentariosRespVisible("Excelente");
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaRepository.findById(40L)).thenReturn(Optional.of(respuestaCorrecta));
        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class)))
                .thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(100L); return r; });

        RespAlumnoGeneralCreateResponse resultado = service.crearRespAlumnoGeneral(10L, 40L, 30L);

        assertThat(resultado.getComentario()).isEqualTo("Excelente");
    }

    @Test
    void crearRespAlumnoGeneral_respVisibleFalse_devuelveComentarioVacio() {
        actividad.setRespVisible(false);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaRepository.findById(40L)).thenReturn(Optional.of(respuestaCorrecta));
        when(respAlumnoGeneralRepository.save(any(RespAlumnoGeneral.class)))
                .thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(100L); return r; });

        RespAlumnoGeneralCreateResponse resultado = service.crearRespAlumnoGeneral(10L, 40L, 30L);

        assertThat(resultado.getComentario()).isEmpty();
    }

    @Test
    void crearRespAlumnoGeneral_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(10L, 40L, 30L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede crear respuestas de alumno");

        verify(respAlumnoGeneralRepository, never()).save(any());
        verify(actividadAlumnoRepository, never()).findById(anyLong());
    }

    @Test
    void crearRespAlumnoGeneral_actividadAlumnoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(99L, 40L, 30L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad del alumno no existe");

        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    @Test
    void crearRespAlumnoGeneral_preguntaNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(10L, 40L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La pregunta no existe");

        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    @Test
    void crearRespAlumnoGeneral_respuestaNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRespAlumnoGeneral(10L, 99L, 30L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta no existe");

        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    // ==================== readRespAlumnoGeneral ====================

    @Test
    void readRespAlumnoGeneral_existente_retornaEntidad() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(true, actividadAlumno, "París", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));

        RespAlumnoGeneral resultado = service.readRespAlumnoGeneral(100L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getCorrecta()).isTrue();
    }

    @Test
    void readRespAlumnoGeneral_noExiste_lanzaRuntimeException() {
        when(respAlumnoGeneralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readRespAlumnoGeneral(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");
    }

    // ==================== updateRespAlumnoGeneral ====================

    @Test
    void updateRespAlumnoGeneral_existente_actualizaCamposYGuarda() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "Madrid", pregunta);
        resp.setId(100L);
        Pregunta nuevaPregunta = new Pregunta();
        nuevaPregunta.setId(31L);
        nuevaPregunta.setActividad(actividad);

        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(preguntaRepository.findById(31L)).thenReturn(Optional.of(nuevaPregunta));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RespAlumnoGeneral resultado = service.updateRespAlumnoGeneral(100L, true, 10L, "París", 31L);

        assertThat(resultado.getCorrecta()).isTrue();
        assertThat(resultado.getRespuesta()).isEqualTo("París");
        assertThat(resultado.getPregunta().getId()).isEqualTo(31L);
        verify(respAlumnoGeneralRepository).save(resp);
    }

    @Test
    void updateRespAlumnoGeneral_noExiste_lanzaRuntimeException() {
        when(respAlumnoGeneralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRespAlumnoGeneral(99L, true, 10L, "París", 30L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");
    }

    @Test
    void updateRespAlumnoGeneral_preguntaNoExiste_lanzaRuntimeException() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "Madrid", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRespAlumnoGeneral(100L, true, 10L, "París", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La pregunta no existe");
    }

    // ==================== deleteRespAlumnoGeneral ====================

    @Test
    void deleteRespAlumnoGeneral_existente_eliminaCorrectamente() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(true, actividadAlumno, "París", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));

        service.deleteRespAlumnoGeneral(100L);

        verify(respAlumnoGeneralRepository).delete(resp);
    }

    @Test
    void deleteRespAlumnoGeneral_noExiste_lanzaRuntimeException() {
        when(respAlumnoGeneralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRespAlumnoGeneral(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno no existe");

        verify(respAlumnoGeneralRepository, never()).delete(any());
    }

    // ==================== corregirRespuestaAlumnoGeneral ====================

    @Test
    void corregirRespuestaAlumnoGeneral_respuestaCoincideConCorrecta_retornaTrue() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "París", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta, respuestaIncorrecta));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boolean resultado = service.corregirRespuestaAlumnoGeneral(100L);

        assertThat(resultado).isTrue();
        assertThat(resp.getCorrecta()).isTrue();
        verify(respAlumnoGeneralRepository).save(resp);
    }

    @Test
    void corregirRespuestaAlumnoGeneral_respuestaNoCoincide_retornaFalse() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "Berlín", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta, respuestaIncorrecta));

        Boolean resultado = service.corregirRespuestaAlumnoGeneral(100L);

        assertThat(resultado).isFalse();
        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    @Test
    void corregirRespuestaAlumnoGeneral_coincideConIncorrectaDelMaestro_retornaFalse() {
        // La respuesta del alumno coincide con una opción del maestro marcada como incorrecta
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "Madrid", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta, respuestaIncorrecta));

        Boolean resultado = service.corregirRespuestaAlumnoGeneral(100L);

        assertThat(resultado).isFalse();
        verify(respAlumnoGeneralRepository, never()).save(any());
    }

    @Test
    void corregirRespuestaAlumnoGeneral_noExiste_lanzaResourceNotFoundException() {
        when(respAlumnoGeneralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.corregirRespuestaAlumnoGeneral(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void corregirRespuestaAlumnoGeneral_sinRespuestasMaestro_retornaFalse() {
        RespAlumnoGeneral resp = new RespAlumnoGeneral(false, actividadAlumno, "París", pregunta);
        resp.setId(100L);
        when(respAlumnoGeneralRepository.findById(100L)).thenReturn(Optional.of(resp));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of());

        Boolean resultado = service.corregirRespuestaAlumnoGeneral(100L);

        assertThat(resultado).isFalse();
    }

    // ==================== corregirCrucigrama ====================

    @Test
    void corregirCrucigrama_todasCorrectas_notaYPuntuacionMaximas() {
        actividad.setTipo(TipoActGeneral.CRUCIGRAMA);
        actividad.setRespVisible(false);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "París"); // correcta

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("París");
        rm.setCorrecta(true);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        // nota = 10/1 = 10, puntuacion = 100/1 = 100
        assertThat(resultado).containsKey(-1L);
        assertThat(resultado.get(-1L)).contains("Nota final: 10");
        assertThat(actividadAlumno.getNota()).isEqualTo(10);
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(100);
        assertThat(actividadAlumno.getFechaFin()).isNotNull();
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void corregirCrucigrama_todasIncorrectas_notaYPuntuacionCeroMinimo() {
        actividad.setTipo(TipoActGeneral.CRUCIGRAMA);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Berlín"); // incorrecta

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("París");
        rm.setCorrecta(true);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        // nota y puntuacion no bajan de 0
        assertThat(actividadAlumno.getNota()).isEqualTo(0);
        assertThat(actividadAlumno.getPuntuacion()).isEqualTo(0);
        assertThat(resultado.get(-1L)).contains("Nota final: 0");
    }

    @Test
    void corregirCrucigrama_actividadAlumnoNoExiste_creaUnaNueva() {
        actividad.setTipo(TipoActGeneral.CRUCIGRAMA);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "París");

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("París");
        rm.setCorrecta(true);

        ActividadAlumno nueva = new ActividadAlumno();
        nueva.setId(99L);
        nueva.setActividad(actividad);
        // numFallos is calculated from respuestasAlumno list, not set directly
        nueva.setNumAbandonos(0);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.empty());
        when(actividadRepository.findByID(50L)).thenReturn(actividad);
        when(actividadAlumnoRepository.save(any(ActividadAlumno.class))).thenReturn(nueva);
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        assertThat(resultado).containsKey(-1L);
    }

    @Test
    void corregirCrucigrama_respuestaConEspaciosYMayusculas_seNormalizaAntesDeComparar() {
        actividad.setTipo(TipoActGeneral.CRUCIGRAMA);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "  PARIS  ");

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("paris");
        rm.setCorrecta(true);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        assertThat(actividadAlumno.getNota()).isEqualTo(10);
    }

    @Test
    void corregirCrucigrama_preguntaNoPerteneceCrucigrama_lanzaIllegalArgumentException() {
        actividad.setTipo(TipoActGeneral.CRUCIGRAMA);

        General otraActividad = new General();
        otraActividad.setId(999L);
        Pregunta preguntaAjena = new Pregunta();
        preguntaAjena.setId(31L);
        preguntaAjena.setActividad(otraActividad);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(31L, "París");

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(31L)).thenReturn(Optional.of(preguntaAjena));

        assertThatThrownBy(() -> service.corregirCrucigrama(respuestas, 50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece al crucigrama");
    }

    @Test
    void corregirCrucigrama_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.corregirCrucigrama(new LinkedHashMap<>(), 50L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede responder crucigramas");

        verify(actividadAlumnoRepository, never()).save(any());
    }

    @Test
    void corregirCrucigrama_respVisible_resultadoContieneRespuestaCorrecta() {
        actividad.setRespVisible(true);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Berlín"); // incorrecta

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("París");
        rm.setCorrecta(true);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        assertThat(resultado.get(30L)).contains("París");
    }

    @Test
    void corregirCrucigrama_respNoVisible_resultadoOculto() {
        actividad.setRespVisible(false);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Berlín");

        RespuestaMaestro rm = new RespuestaMaestro();
        rm.setRespuesta("París");
        rm.setCorrecta(true);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(actividadAlumnoRepository.findByAlumnoIdAndActividadId(1L, 50L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(rm));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        var resultado = service.corregirCrucigrama(respuestas, 50L);

        assertThat(resultado.get(30L)).doesNotContain("París");
    }

    // ==================== corregirActividadAbierta ====================

    @Test
    void corregirActividadAbierta_unaRespuesta_calculaNotaYPuntuacion() {
        actividad.setTipo(TipoActGeneral.ABIERTA);
        actividad.setRespVisible(false);
        actividad.setPuntuacion(100);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Respuesta del alumno");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta));
        when(iaConnectionService.evaluarRespuestaAbierta(any(), any(), any(), any()))
                .thenReturn(Map.of("puntuacion", 80, "comentarios", "Bien respondido"));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        EvaluacionActividadAbiertaResponse resultado = service.corregirActividadAbierta(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getPuntuacionFinal()).isEqualTo(80);
        assertThat(resultado.getNotaFinal()).isEqualTo(8); // round(80/100 * 10)
        assertThat(resultado.getDetallesPreguntas()).hasSize(1);
        assertThat(actividadAlumno.getFechaFin()).isNotNull();
        verify(actividadAlumnoRepository).save(actividadAlumno);
    }

    @Test
    void corregirActividadAbierta_respVisibleTrue_devuelveComentariosIA() {
        actividad.setTipo(TipoActGeneral.ABIERTA);
        actividad.setRespVisible(true);
        actividad.setComentariosRespVisible("Genial");

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Respuesta alumno");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta));
        when(iaConnectionService.evaluarRespuestaAbierta(any(), any(), any(), any()))
                .thenReturn(Map.of("puntuacion", 50, "comentarios", "Comentario IA"));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        EvaluacionActividadAbiertaResponse resultado = service.corregirActividadAbierta(request);

        assertThat(resultado.getDetallesPreguntas().get(0).getComentarios()).isEqualTo("Comentario IA");
        assertThat(resultado.getDetallesPreguntas().get(0).getRespVisible()).isTrue();
        assertThat(resultado.getDetallesPreguntas().get(0).getComentariosRespVisible()).isEqualTo("Genial");
    }

    @Test
    void corregirActividadAbierta_respVisibleFalse_ocultaComentariosIA() {
        actividad.setTipo(TipoActGeneral.ABIERTA);
        actividad.setRespVisible(false);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Respuesta alumno");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta));
        when(iaConnectionService.evaluarRespuestaAbierta(any(), any(), any(), any()))
                .thenReturn(Map.of("puntuacion", 50, "comentarios", "Comentario IA"));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        EvaluacionActividadAbiertaResponse resultado = service.corregirActividadAbierta(request);

        assertThat(resultado.getDetallesPreguntas().get(0).getComentarios())
                .isEqualTo("Corrección oculta por configuración de la actividad.");
    }

    @Test
    void corregirActividadAbierta_puntuacionIASuperaMaximo_seLimitaAlMaximo() {
        actividad.setTipo(TipoActGeneral.ABIERTA);
        actividad.setRespVisible(false);
        actividad.setPuntuacion(100);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Respuesta alumno");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta));
        when(iaConnectionService.evaluarRespuestaAbierta(any(), any(), any(), any()))
                .thenReturn(Map.of("puntuacion", 9999, "comentarios", "Excelente"));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        EvaluacionActividadAbiertaResponse resultado = service.corregirActividadAbierta(request);

        // maxPuntuacionPorPregunta = 100/1 = 100, no puede superar 100
        assertThat(resultado.getPuntuacionFinal()).isEqualTo(100);
        assertThat(resultado.getNotaFinal()).isEqualTo(10);
    }

    @Test
    void corregirActividadAbierta_puntuacionIANegativa_seLimitaACero() {
        actividad.setTipo(TipoActGeneral.ABIERTA);
        actividad.setRespVisible(false);
        actividad.setPuntuacion(100);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(30L, "Respuesta alumno");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(30L)).thenReturn(Optional.of(pregunta));
        when(respuestaService.encontrarRespuestasPorPreguntaId(30L)).thenReturn(List.of(respuestaCorrecta));
        when(iaConnectionService.evaluarRespuestaAbierta(any(), any(), any(), any()))
                .thenReturn(Map.of("puntuacion", -50, "comentarios", "Muy mal"));
        when(respAlumnoGeneralRepository.save(any())).thenAnswer(inv -> { RespAlumnoGeneral r = inv.getArgument(0); r.setId(200L); return r; });

        EvaluacionActividadAbiertaResponse resultado = service.corregirActividadAbierta(request);

        assertThat(resultado.getPuntuacionFinal()).isEqualTo(0);
        assertThat(resultado.getNotaFinal()).isEqualTo(0);
    }

    @Test
    void corregirActividadAbierta_actividadNoEsTipoAbierta_lanzaIllegalArgumentException() {
        actividad.setTipo(TipoActGeneral.TEST);

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, new LinkedHashMap<>());

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));

        assertThatThrownBy(() -> service.corregirActividadAbierta(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La actividad no es de tipo abierta");
    }

    @Test
    void corregirActividadAbierta_usuarioNoAlumno_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, new LinkedHashMap<>());

        assertThatThrownBy(() -> service.corregirActividadAbierta(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede enviar respuestas de la actividad");

        verify(actividadAlumnoRepository, never()).findById(anyLong());
    }

    @Test
    void corregirActividadAbierta_actividadAlumnoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(99L)).thenReturn(Optional.empty());

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(99L, new LinkedHashMap<>());

        assertThatThrownBy(() -> service.corregirActividadAbierta(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad del alumno no existe");
    }

    @Test
    void corregirActividadAbierta_preguntaNoPertenecAActividad_lanzaIllegalArgumentException() {
        actividad.setTipo(TipoActGeneral.ABIERTA);

        General otraActividad = new General();
        otraActividad.setId(999L);
        Pregunta preguntaAjena = new Pregunta();
        preguntaAjena.setId(31L);
        preguntaAjena.setActividad(otraActividad);

        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(31L, "Respuesta");

        EvaluacionActividadAbiertaRequest request =
                new EvaluacionActividadAbiertaRequest(10L, respuestas);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadAlumnoRepository.findById(10L)).thenReturn(Optional.of(actividadAlumno));
        when(preguntaRepository.findById(31L)).thenReturn(Optional.of(preguntaAjena));

        assertThatThrownBy(() -> service.corregirActividadAbierta(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la actividad");
    }
}