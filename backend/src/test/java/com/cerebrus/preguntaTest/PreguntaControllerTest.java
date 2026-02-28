package com.cerebrus.preguntaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaController;
import com.cerebrus.pregunta.PreguntaService;
import com.cerebrus.respuesta.Respuesta;

@ExtendWith(MockitoExtension.class)
class PreguntaControllerTest {

    @Mock
    private PreguntaService preguntaService;

    @InjectMocks
    private PreguntaController preguntaController;

    private Actividad actividad;
    private Pregunta preguntaRequest;
    private Pregunta preguntaGuardada;

    @BeforeEach
    void setUp() {
        // Actividad es abstract, se instancia con clase anónima
        actividad = new Actividad() {};
        actividad.setId(1L);

        preguntaRequest = new Pregunta("¿Cuánto es 2+2?", "img.png", actividad);

        preguntaGuardada = new Pregunta("¿Cuánto es 2+2?", "img.png", actividad);
        preguntaGuardada.setId(10L);
    }

    // Test para verificar que crearPregunta retorna 201 CREATED con el cuerpo de la pregunta creada
    @Test
    void crearPregunta_requestValido_retorna201ConPregunta() {
        when(preguntaService.crearPregunta("¿Cuánto es 2+2?", "img.png", 1L)).thenReturn(preguntaGuardada);

        ResponseEntity<Pregunta> respuesta = preguntaController.crearPregunta(preguntaRequest);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getPregunta()).isEqualTo("¿Cuánto es 2+2?");
        verify(preguntaService).crearPregunta("¿Cuánto es 2+2?", "img.png", 1L);
    }

    // Test para verificar que crearPregunta retorna 201 cuando la imagen es null
    @Test
    void crearPregunta_sinImagen_retorna201() {
        Pregunta reqSinImagen = new Pregunta("¿Capital de Francia?", null, actividad);
        Pregunta savedSinImagen = new Pregunta("¿Capital de Francia?", null, actividad);
        savedSinImagen.setId(11L);
        when(preguntaService.crearPregunta("¿Capital de Francia?", null, 1L)).thenReturn(savedSinImagen);

        ResponseEntity<Pregunta> respuesta = preguntaController.crearPregunta(reqSinImagen);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody().getImagen()).isNull();
    }

    // Test para verificar que crearPregunta propaga AccessDeniedException cuando el service la lanza
    @Test
    void crearPregunta_accesoNoPermitido_propagaAccessDeniedException() {
        when(preguntaService.crearPregunta(any(), any(), any()))
                .thenThrow(new AccessDeniedException("Solo un maestro puede crear preguntas"));

        assertThatThrownBy(() -> preguntaController.crearPregunta(preguntaRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear preguntas");
    }

    // Test para verificar que crearPregunta propaga ResourceNotFoundException cuando la actividad no existe
    @Test
    void crearPregunta_actividadNoExiste_propagaResourceNotFoundException() {
        when(preguntaService.crearPregunta(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("La actividad de la pregunta no existe"));

        assertThatThrownBy(() -> preguntaController.crearPregunta(preguntaRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad de la pregunta no existe");
    }

    // Test para verificar que readPregunta retorna 200 OK con la pregunta y sus respuestas
    @Test
    void readPregunta_existente_retorna200ConPregunta() {
        Respuesta r1 = new Respuesta("4", null, true, preguntaGuardada);
        Respuesta r2 = new Respuesta("5", null, false, preguntaGuardada);
        preguntaGuardada.setRespuestas(new ArrayList<>(List.of(r1, r2)));
        when(preguntaService.readPregunta(10L)).thenReturn(preguntaGuardada);

        ResponseEntity<Pregunta> respuesta = preguntaController.readPregunta(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getRespuestas()).hasSize(2);
    }

    // Test para verificar que readPregunta retorna 200 OK con lista de respuestas vacía cuando la pregunta no tiene respuestas
    @Test
    void readPregunta_sinRespuestas_retorna200ConListaVacia() {
        preguntaGuardada.setRespuestas(new ArrayList<>());
        when(preguntaService.readPregunta(10L)).thenReturn(preguntaGuardada);

        ResponseEntity<Pregunta> respuesta = preguntaController.readPregunta(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getRespuestas()).isEmpty();
    }

    // Test para verificar que readPregunta propaga ResourceNotFoundException cuando la pregunta no existe
    @Test
    void readPregunta_noExiste_propagaResourceNotFoundException() {
        when(preguntaService.readPregunta(99L))
                .thenThrow(new ResourceNotFoundException("La pregunta no existe"));

        assertThatThrownBy(() -> preguntaController.readPregunta(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");
    }

    // Test para verificar que updatePregunta retorna 200 OK con la pregunta actualizada
    @Test
    void updatePregunta_existente_retorna200ConPreguntaActualizada() {
        Pregunta bodyRequest = new Pregunta("¿Nueva?", "nueva.png", actividad);
        Pregunta actualizada = new Pregunta("¿Nueva?", "nueva.png", actividad);
        actualizada.setId(10L);
        when(preguntaService.updatePregunta(10L, "¿Nueva?", "nueva.png")).thenReturn(actualizada);

        ResponseEntity<Pregunta> respuesta = preguntaController.updatePregunta(10L, bodyRequest);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getPregunta()).isEqualTo("¿Nueva?");
        assertThat(respuesta.getBody().getImagen()).isEqualTo("nueva.png");
    }

    // Test para verificar que updatePregunta retorna 200 OK cuando la imagen es null
    @Test
    void updatePregunta_imagenNull_retorna200() {
        Pregunta bodyRequest = new Pregunta("Texto nuevo", null, actividad);
        Pregunta actualizada = new Pregunta("Texto nuevo", null, actividad);
        actualizada.setId(10L);
        when(preguntaService.updatePregunta(10L, "Texto nuevo", null)).thenReturn(actualizada);

        ResponseEntity<Pregunta> respuesta = preguntaController.updatePregunta(10L, bodyRequest);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getImagen()).isNull();
    }

    // Test para verificar que updatePregunta propaga AccessDeniedException cuando el service la lanza
    @Test
    void updatePregunta_accesoNoPermitido_propagaAccessDeniedException() {
        when(preguntaService.updatePregunta(any(), any(), any()))
                .thenThrow(new AccessDeniedException("Solo un maestro puede actualizar preguntas"));

        assertThatThrownBy(() -> preguntaController.updatePregunta(10L, preguntaRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar preguntas");
    }

    // Test para verificar que updatePregunta propaga ResourceNotFoundException cuando la pregunta no existe
    @Test
    void updatePregunta_noExiste_propagaResourceNotFoundException() {
        when(preguntaService.updatePregunta(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("La pregunta no existe"));

        assertThatThrownBy(() -> preguntaController.updatePregunta(99L, preguntaRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");
    }

    // Test para verificar que deletePregunta retorna 204 NO CONTENT sin cuerpo cuando la eliminación es exitosa
    @Test
    void deletePregunta_existente_retorna204SinCuerpo() {
        ResponseEntity<Void> respuesta = preguntaController.deletePregunta(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(respuesta.getBody()).isNull();
        verify(preguntaService).deletePregunta(10L);
    }

    // Test para verificar que deletePregunta propaga AccessDeniedException cuando el service la lanza
    @Test
    void deletePregunta_accesoNoPermitido_propagaAccessDeniedException() {
        doThrow(new AccessDeniedException("Solo un maestro puede eliminar preguntas"))
                .when(preguntaService).deletePregunta(10L);

        assertThatThrownBy(() -> preguntaController.deletePregunta(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar preguntas");
    }

    // Test para verificar que deletePregunta propaga ResourceNotFoundException cuando la pregunta no existe
    @Test
    void deletePregunta_noExiste_propagaResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("La pregunta no existe"))
                .when(preguntaService).deletePregunta(99L);

        assertThatThrownBy(() -> preguntaController.deletePregunta(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La pregunta no existe");
    }
}
