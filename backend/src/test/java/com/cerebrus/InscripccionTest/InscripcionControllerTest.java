package com.cerebrus.InscripccionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.inscripcion.InscripcionController;
import com.cerebrus.inscripcion.InscripcionService;

@ExtendWith(MockitoExtension.class)
class InscripcionControllerTest {

    @Mock
    private InscripcionService inscripcionService;

    @InjectMocks
    private InscripcionController controller;

    // ==================== inscribirAlumno ====================

    @Test
    void inscribirAlumno_codigoValido_retorna200ConMensajeExito() {
        when(inscripcionService.crearInscripcion("ABC123")).thenReturn(new Inscripcion());

        ResponseEntity<String> respuesta = controller.inscribirAlumno("ABC123");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo("¡Alumno inscrito correctamente en el curso!");
        verify(inscripcionService).crearInscripcion("ABC123");
    }

    @Test
    void inscribirAlumno_cursoNoExiste_retorna404ConMensaje() {
        when(inscripcionService.crearInscripcion("NOPE")).thenThrow(new RuntimeException("404 Not Found"));

        ResponseEntity<String> respuesta = controller.inscribirAlumno("NOPE");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isEqualTo("El curso no existe");
    }

    @Test
    void inscribirAlumno_alumnoYaInscrito_retorna400ConMensaje() {
        when(inscripcionService.crearInscripcion("ABC123")).thenThrow(new RuntimeException("400 Bad Request"));

        ResponseEntity<String> respuesta = controller.inscribirAlumno("ABC123");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isEqualTo("El alumno ya está inscrito en este curso");
    }

    @Test
    void inscribirAlumno_cursoNoVisible_retorna403ConMensaje() {
        when(inscripcionService.crearInscripcion("ABC123")).thenThrow(new RuntimeException("403 Forbidden"));

        ResponseEntity<String> respuesta = controller.inscribirAlumno("ABC123");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(respuesta.getBody()).isEqualTo("No puedes unirte a un curso no visible");
    }

    @Test
    void inscribirAlumno_usuarioNoAutorizado_retorna401ConMensaje() {
        when(inscripcionService.crearInscripcion("ABC123")).thenThrow(new RuntimeException("401 Unauthorized"));

        ResponseEntity<String> respuesta = controller.inscribirAlumno("ABC123");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody()).isEqualTo("No autorizado");
    }

    @Test
    void inscribirAlumno_errorInesperado_retorna500ConMensaje() {
        when(inscripcionService.crearInscripcion("ABC123")).thenThrow(new RuntimeException("error desconocido"));

        ResponseEntity<String> respuesta = controller.inscribirAlumno("ABC123");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).isEqualTo("Error interno");
    }
}
