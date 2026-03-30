package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionCreateResponse;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionDTO;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class RespAlumnoOrdenacionControllerTest {

    @Mock
    private RespAlumnoOrdenacionService respAlumnoOrdenacionService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private RespAlumnoOrdenacionController controller;

    private Alumno alumno;
    private Maestro maestro;
    private RespAlumnoOrdenacion respAlumnoOrdenacion;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);

        maestro = new Maestro();
        maestro.setId(2L);

        ActividadAlumno actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(10L);

        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(20L);

        respAlumnoOrdenacion = new RespAlumnoOrdenacion();
        respAlumnoOrdenacion.setActividadAlumno(actividadAlumno);
        respAlumnoOrdenacion.setOrdenacion(ordenacion);
        respAlumnoOrdenacion.setValoresAlum(List.of("A", "B", "C"));
    }

    // ==================== crearRespuestaAlumnoOrdenacion ====================

    @Test
    void crearRespuestaAlumnoOrdenacion_alumnoValido_retorna201ConRespuesta() {
        RespAlumnoOrdenacionDTO dto = new RespAlumnoOrdenacionDTO(100L, true);
        RespAlumnoOrdenacionCreateResponse respuesta = new RespAlumnoOrdenacionCreateResponse(dto, "Bien hecho");
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L))
                .thenReturn(respuesta);

        ResponseEntity<RespAlumnoOrdenacionCreateResponse> resultado =
                controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getRespAlumnoOrdenacion().getCorrecta()).isTrue();
        assertThat(resultado.getBody().getComentario()).isEqualTo("Bien hecho");
        verify(respAlumnoOrdenacionService).crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L);
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_respuestaIncorrecta_retorna201ConCorrectaFalse() {
        RespAlumnoOrdenacionDTO dto = new RespAlumnoOrdenacionDTO(101L, false);
        RespAlumnoOrdenacionCreateResponse respuesta = new RespAlumnoOrdenacionCreateResponse(dto, "");
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L))
                .thenReturn(respuesta);

        ResponseEntity<RespAlumnoOrdenacionCreateResponse> resultado =
                controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody().getRespAlumnoOrdenacion().getCorrecta()).isFalse();
        assertThat(resultado.getBody().getComentario()).isEmpty();
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_usuarioNoAlumno_propagaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede crear respuestas de alumno");
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_actividadAlumnoNoExiste_propagaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(anyLong(), anyList(), anyLong()))
                .thenThrow(new RuntimeException("La actividad del alumno no existe"));

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad del alumno no existe");
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_ordenacionNoExiste_propagaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(anyLong(), anyList(), anyLong()))
                .thenThrow(new RuntimeException("La actividad de ordenación no existe"));

        assertThatThrownBy(() -> controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La actividad de ordenación no existe");
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_comentarioNullCuandoRespNoVisible_retorna201ConComentarioVacio() {
        RespAlumnoOrdenacionDTO dto = new RespAlumnoOrdenacionDTO(102L, true);
        RespAlumnoOrdenacionCreateResponse respuesta = new RespAlumnoOrdenacionCreateResponse(dto, "");
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(10L, List.of("A", "B", "C"), 20L))
                .thenReturn(respuesta);

        ResponseEntity<RespAlumnoOrdenacionCreateResponse> resultado =
                controller.crearRespuestaAlumnoOrdenacion(respAlumnoOrdenacion);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody().getComentario()).isEmpty();
    }

    // ==================== encontrarRespuestaAlumnoOrdenacionPorId ====================

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_existente_retorna200ConDTO() {
        RespAlumnoOrdenacion resp = new RespAlumnoOrdenacion();
        resp.setId(100L);
        resp.setCorrecta(true);
        when(respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(100L)).thenReturn(resp);

        ResponseEntity<RespAlumnoOrdenacionDTO> resultado = controller.encontrarRespuestaAlumnoOrdenacionPorId(100L);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().getId()).isEqualTo(100L);
        assertThat(resultado.getBody().getCorrecta()).isTrue();
        verify(respAlumnoOrdenacionService).encontrarRespuestaAlumnoOrdenacionPorId(100L);
    }

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_correctaFalse_retorna200ConCorrectaFalse() {
        RespAlumnoOrdenacion resp = new RespAlumnoOrdenacion();
        resp.setId(101L);
        resp.setCorrecta(false);
        when(respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(101L)).thenReturn(resp);

        ResponseEntity<RespAlumnoOrdenacionDTO> resultado = controller.encontrarRespuestaAlumnoOrdenacionPorId(101L);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody().getCorrecta()).isFalse();
    }

    @Test
    void encontrarRespuestaAlumnoOrdenacionPorId_noExiste_propagaRuntimeException() {
        when(respAlumnoOrdenacionService.encontrarRespuestaAlumnoOrdenacionPorId(99L))
                .thenThrow(new RuntimeException("La respuesta del alumno a la actividad de ordenación no existe"));

        assertThatThrownBy(() -> controller.encontrarRespuestaAlumnoOrdenacionPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La respuesta del alumno a la actividad de ordenación no existe");
    }
}
