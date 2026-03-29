package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.dto.RespAlumnoPuntoImagenDTO;

@ExtendWith(MockitoExtension.class)
class RespAlumnoPuntoImagenControllerTest {

    @Mock
    private RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @InjectMocks
    private RespAlumnoPuntoImagenController controller;

    private RespAlumnoPuntoImagenDTO requestDTO;
    private RespAlumnoPuntoImagen entidadGuardada;

    @BeforeEach
    void setUp() {
        requestDTO = new RespAlumnoPuntoImagenDTO(null, "París", 5L, 10L);

        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setId(5L);

        ActividadAlumno actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);

        entidadGuardada = new RespAlumnoPuntoImagen();
        entidadGuardada.setId(100L);
        entidadGuardada.setRespuesta("París");
        entidadGuardada.setPuntoImagen(puntoImagen);
        entidadGuardada.setActividadAlumno(actividadAlumno);
        entidadGuardada.setCorrecta(false);
    }

    // ==================== crearRespuestaAlumnoPuntoImagen ====================

    @Test
    void crearRespuestaAlumnoPuntoImagen_datosValidos_retorna201ConDTO() {
        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("París", 5L, 10L))
                .thenReturn(entidadGuardada);

        ResponseEntity<RespAlumnoPuntoImagenDTO> resultado =
                controller.crearRespuestaAlumnoPuntoImagen(requestDTO);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getId()).isEqualTo(100L);
        assertThat(resultado.getBody().getRespuesta()).isEqualTo("París");
        assertThat(resultado.getBody().getPuntoImagenId()).isEqualTo(5L);
        assertThat(resultado.getBody().getActividadAlumnoId()).isEqualTo(10L);
        verify(respAlumnoPuntoImagenService).crearRespuestaAlumnoPuntoImagen("París", 5L, 10L);
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_respuestaVacia_retorna201ConDTOVacio() {
        RespAlumnoPuntoImagenDTO reqVacio = new RespAlumnoPuntoImagenDTO(null, "", 5L, 10L);
        entidadGuardada.setRespuesta("");
        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("", 5L, 10L))
                .thenReturn(entidadGuardada);

        ResponseEntity<RespAlumnoPuntoImagenDTO> resultado =
                controller.crearRespuestaAlumnoPuntoImagen(reqVacio);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody().getRespuesta()).isEmpty();
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_usuarioNoAlumno_propagaAccessDeniedException() {
        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen(anyString(), anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Solo un alumno puede crear respuestas para puntos de imagen"));

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoPuntoImagen(requestDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede crear respuestas para puntos de imagen");
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_puntoImagenNoExiste_propagaResourceNotFoundException() {
        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen(anyString(), anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("PuntoImagen", "id", 5L));

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoPuntoImagen(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_actividadAlumnoNoExiste_propagaRuntimeException() {
        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen(anyString(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Actividad alumno no encontrada"));

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoPuntoImagen(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Actividad alumno no encontrada");
    }
}
