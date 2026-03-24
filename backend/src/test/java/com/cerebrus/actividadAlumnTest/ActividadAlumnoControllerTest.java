package com.cerebrus.actividadAlumnTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoController;
import com.cerebrus.actividadAlumn.ActividadAlumnoService;
import com.cerebrus.actividadAlumn.dto.ActividadAlumnoDTO;
import com.cerebrus.actividadAlumn.dto.CorreccionManualDTO;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.alumno.Alumno;

@ExtendWith(MockitoExtension.class)
class ActividadAlumnoControllerTest {

    @Mock
    private ActividadAlumnoService actividadAlumnoService;

    @InjectMocks
    private ActividadAlumnoController controller;

    private Alumno alumno;
    private Actividad actividad;
    private ActividadAlumno actividadAlumno;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);

        actividad = new General();
        actividad.setId(50L);

        actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);
        actividadAlumno.setPuntuacion(100);
        actividadAlumno.setFechaInicio(LocalDateTime.of(2024, 1, 1, 9, 0));
        actividadAlumno.setFechaFin(LocalDateTime.of(2024, 1, 1, 9, 30));
        actividadAlumno.setNota(8);
        actividadAlumno.setNumAbandonos(0);
        actividadAlumno.setAlumno(alumno);
        actividadAlumno.setActividad(actividad);
    }

    // ==================== crearActividadAlumno ====================

    @Test
    void crearActividadAlumno_datosCompletos_retorna201ConDTO() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, 100, LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 9, 30), 8, 0, 1L, 50L);
        when(actividadAlumnoService.crearActividadAlumno(
                eq(100), any(LocalDateTime.class), any(LocalDateTime.class),
                eq(8), eq(0), eq(1L), eq(50L)))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.crearActividadAlumno(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getPuntuacion()).isEqualTo(100);
        assertThat(respuesta.getBody().getNota()).isEqualTo(8);
        assertThat(respuesta.getBody().getAlumnoId()).isEqualTo(1L);
        assertThat(respuesta.getBody().getActividadId()).isEqualTo(50L);
    }

    @Test
    void crearActividadAlumno_puntuacionNull_usaCero() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, null, LocalDateTime.now(), LocalDateTime.now(), null, null, 1L, 50L);
        actividadAlumno.setPuntuacion(0);
        actividadAlumno.setNota(0);
        actividadAlumno.setNumAbandonos(0);
        when(actividadAlumnoService.crearActividadAlumno(
                eq(0), any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(0), eq(1L), eq(50L)))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.crearActividadAlumno(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody().getPuntuacion()).isEqualTo(0);
    }

    @Test
    void crearActividadAlumno_actividadNoExiste_propagaResourceNotFoundException() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, 100, LocalDateTime.now(), LocalDateTime.now(), 8, 0, 1L, 99L);
        when(actividadAlumnoService.crearActividadAlumno(
                anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("La actividad no existe"));

        assertThatThrownBy(() -> controller.crearActividadAlumno(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearActividadAlumno_parejaYaExiste_retorna201ConExistente() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, 100, LocalDateTime.now(), LocalDateTime.now(), 8, 0, 1L, 50L);
        when(actividadAlumnoService.crearActividadAlumno(
                anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong()))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.crearActividadAlumno(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
    }

    // ==================== readActividadAlumno ====================

    @Test
    void readActividadAlumno_existente_retorna200ConDTO() {
        when(actividadAlumnoService.readActividadAlumno(10L)).thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.readActividadAlumno(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getNota()).isEqualTo(8);
    }

    @Test
    void readActividadAlumno_noExiste_propagaResourceNotFoundException() {
        when(actividadAlumnoService.readActividadAlumno(99L))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.readActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateActividadAlumno ====================

    @Test
    void updateActividadAlumno_datosValidos_retorna200ConDTOActualizado() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, 200, LocalDateTime.now(), LocalDateTime.now(), 9, 1, 1L, 50L);
        actividadAlumno.setPuntuacion(200);
        actividadAlumno.setNota(9);
        actividadAlumno.setNumAbandonos(1);
        when(actividadAlumnoService.updateActividadAlumno(
                eq(10L), eq(200), any(), any(), eq(9), eq(1)))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.updateActividadAlumno(10L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getPuntuacion()).isEqualTo(200);
        assertThat(respuesta.getBody().getNota()).isEqualTo(9);
        assertThat(respuesta.getBody().getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoService).updateActividadAlumno(eq(10L), eq(200), any(), any(), eq(9), eq(1));
    }

    @Test
    void updateActividadAlumno_noExiste_propagaResourceNotFoundException() {
        ActividadAlumnoDTO request = new ActividadAlumnoDTO(
                null, 100, LocalDateTime.now(), LocalDateTime.now(), 8, 0, 1L, 50L);
        when(actividadAlumnoService.updateActividadAlumno(
                eq(99L), any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.updateActividadAlumno(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== deleteActividadAlumno ====================

    @Test
    void deleteActividadAlumno_existente_retorna204SinCuerpo() {
        ResponseEntity<Void> respuesta = controller.deleteActividadAlumno(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(respuesta.getBody()).isNull();
        verify(actividadAlumnoService).deleteActividadAlumno(10L);
    }

    @Test
    void deleteActividadAlumno_noExiste_propagaResourceNotFoundException() {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("La actividad del alumno no existe"))
                .when(actividadAlumnoService).deleteActividadAlumno(99L);

        assertThatThrownBy(() -> controller.deleteActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== ensureActividadAlumno ====================

    @Test
    void ensureActividadAlumno_existe_retorna200Con1() {
        when(actividadAlumnoService.ensureActividadAlumno(50L)).thenReturn(1);

        ResponseEntity<Integer> respuesta = controller.ensureActividadAlumno(50L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(1);
    }

    @Test
    void ensureActividadAlumno_noExiste_retorna200Con0() {
        when(actividadAlumnoService.ensureActividadAlumno(50L)).thenReturn(0);

        ResponseEntity<Integer> respuesta = controller.ensureActividadAlumno(50L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(0);
    }

    // ==================== readActividadAlumnoByAlumnoIdAndActividadId ====================

    @Test
    void readActividadAlumnoByAlumnoIdAndActividadId_existe_retorna200ConDTO() {
        when(actividadAlumnoService.readActividadAlumnoByAlumnoIdAndActividadId(1L, 50L))
                .thenReturn(Optional.of(actividadAlumno));

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.readActividadAlumnoByAlumnoIdAndActividadId(1L, 50L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getAlumnoId()).isEqualTo(1L);
        assertThat(respuesta.getBody().getActividadId()).isEqualTo(50L);
    }

    @Test
    void readActividadAlumnoByAlumnoIdAndActividadId_noExiste_retorna404() {
        when(actividadAlumnoService.readActividadAlumnoByAlumnoIdAndActividadId(1L, 99L))
                .thenReturn(Optional.empty());

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.readActividadAlumnoByAlumnoIdAndActividadId(1L, 99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }

    // ==================== abandonarActividadAlumno ====================

    @Test
    void abandonarActividadAlumno_alumnoValido_retorna200ConDTOActualizado() {
        actividadAlumno.setNumAbandonos(1);
        when(actividadAlumnoService.abandonarActividadAlumno(10L)).thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta = controller.abandonarActividadAlumno(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getNumAbandonos()).isEqualTo(1);
        verify(actividadAlumnoService).abandonarActividadAlumno(10L);
    }

    @Test
    void abandonarActividadAlumno_noExiste_propagaResourceNotFoundException() {
        when(actividadAlumnoService.abandonarActividadAlumno(99L))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.abandonarActividadAlumno(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void abandonarActividadAlumno_usuarioNoAlumno_propagaAccessDeniedException() {
        when(actividadAlumnoService.abandonarActividadAlumno(10L))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Solo un alumno puede abandonar su actividad"));

        assertThatThrownBy(() -> controller.abandonarActividadAlumno(10L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    // ==================== corregirActividadAlumnoManual ====================

    @Test
    void corregirActividadAlumnoManual_notaYRespuestas_retorna200ConDTOCorregido() {
        CorreccionManualDTO dto = new CorreccionManualDTO(7, List.of(101L, 102L));
        actividadAlumno.setNota(7);
        when(actividadAlumnoService.corregirActividadAlumnoManual(10L, 7, List.of(101L, 102L)))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoManual(10L, dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getNota()).isEqualTo(7);
        verify(actividadAlumnoService).corregirActividadAlumnoManual(10L, 7, List.of(101L, 102L));
    }

    @Test
    void corregirActividadAlumnoManual_soloNota_retorna200() {
        CorreccionManualDTO dto = new CorreccionManualDTO(5, null);
        actividadAlumno.setNota(5);
        when(actividadAlumnoService.corregirActividadAlumnoManual(10L, 5, null))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoManual(10L, dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getNota()).isEqualTo(5);
    }

    @Test
    void corregirActividadAlumnoManual_noExiste_propagaResourceNotFoundException() {
        CorreccionManualDTO dto = new CorreccionManualDTO(7, List.of(101L));
        when(actividadAlumnoService.corregirActividadAlumnoManual(99L, 7, List.of(101L)))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.corregirActividadAlumnoManual(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActividadAlumnoAutomaticamente ====================

    @Test
    void corregirActividadAlumnoAutomaticamente_conRespuestasIds_retorna200ConDTO() {
        List<Long> ids = List.of(101L, 102L);
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamente(10L, ids))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoAutomaticamente(10L, ids);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        verify(actividadAlumnoService).corregirActividadAlumnoAutomaticamente(10L, ids);
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_sinCuerpo_pasaNull() {
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamente(10L, null))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoAutomaticamente(10L, null);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(actividadAlumnoService).corregirActividadAlumnoAutomaticamente(10L, null);
    }

    @Test
    void corregirActividadAlumnoAutomaticamente_noExiste_propagaResourceNotFoundException() {
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamente(99L, null))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.corregirActividadAlumnoAutomaticamente(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== corregirActividadAlumnoAutomaticamenteGeneralClasificacion ====================

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_conIds_retorna200ConDTO() {
        List<Long> ids = List.of(201L, 202L);
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, ids))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, ids);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        verify(actividadAlumnoService).corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, ids);
    }

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_sinCuerpo_pasaNull() {
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, null))
                .thenReturn(actividadAlumno);

        ResponseEntity<ActividadAlumnoDTO> respuesta =
                controller.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(10L, null);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void corregirActividadAlumnoAutomaticamenteGeneralClasificacion_noExiste_propagaResourceNotFoundException() {
        when(actividadAlumnoService.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(99L, null))
                .thenThrow(new ResourceNotFoundException("La actividad del alumno no existe"));

        assertThatThrownBy(() ->
                controller.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}