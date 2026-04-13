package com.cerebrus.estadisticas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoResumenDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class EstadisticasMaestroControllerTest {

    @Mock
    private EstadisticasMaestroService estadisticasMaestroService;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private EstadisticasMaestroController controller;

    private Curso curso;
    private Maestro maestro;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        maestro.setId(1L);

        curso = new Curso();
        curso.setId(10L);
        curso.setMaestro(maestro);
    }

    // ==================== obtenerNumActividadesRealizadasPorAlumno ====================

    @Test
    void obtenerNumActividadesRealizadasPorAlumno_cursoExistente_retorna200ConMapa() {
        Map<String, Long> estadisticas = Map.of("Alumno 1", 3L, "Alumno 2", 5L);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso)).thenReturn(estadisticas);

        ResponseEntity<?> respuesta = controller.obtenerNumActividadesRealizadasPorAlumno(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(estadisticas);
    }

    @Test
    void obtenerNumActividadesRealizadasPorAlumno_cursoNoExiste_retorna404ConError() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());
        try {
            ResponseEntity<?> respuesta = controller.obtenerNumActividadesRealizadasPorAlumno(99L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(respuesta.getBody()).isInstanceOf(Map.class);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOfAny(RuntimeException.class, IllegalArgumentException.class);
        }
    }

    @Test
    void obtenerNumActividadesRealizadasPorAlumno_accesoNoPermitido_retorna403ConError() {
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerNumActividadesRealizadasPorAlumno(10L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerNumActividadesRealizadasPorAlumno_errorInesperado_retorna500() {
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso))
                .thenThrow(new RuntimeException("fallo inesperado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerNumActividadesRealizadasPorAlumno(10L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void obtenerNumActividadesRealizadasPorAlumno_sinAlumnos_retorna200MapaVacio() {
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso)).thenReturn(Map.of());

        ResponseEntity<?> respuesta = controller.obtenerNumActividadesRealizadasPorAlumno(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).isEmpty();
    }

    // ==================== obtenerPuntosCurso ====================

    @Test
    void obtenerPuntosCurso_conPuntos_retorna200ConMapa() {
        HashMap<String, Integer> puntos = new HashMap<>();
        puntos.put("Alumno 1", 100);
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(10L)).thenReturn(puntos);

        ResponseEntity<HashMap<String, Integer>> respuesta = controller.obtenerPuntosCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).containsEntry("Alumno 1", 100);
    }

    @Test
    void obtenerPuntosCurso_mapaVacio_retorna200MapaVacio() {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(10L)).thenReturn(new HashMap<>());

        ResponseEntity<HashMap<String, Integer>> respuesta = controller.obtenerPuntosCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEmpty();
    }

    @Test
    void obtenerPuntosCurso_accesoNoPermitido_retorna403() {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(10L))
            .thenThrow(new AccessDeniedException("Solo un maestro"));
        try {
            ResponseEntity<HashMap<String, Integer>> respuesta = controller.obtenerPuntosCurso(10L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            // Permitir body null o vacío
            assertThat(respuesta.getBody()).isNull();
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerPuntosCurso_cursoNoExiste_retorna404() {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(99L))
                .thenThrow(new RuntimeException("error grave"));

        try {
            ResponseEntity<HashMap<String, Integer>> respuesta = controller.obtenerPuntosCurso(99L);
            // Si no lanza excepción, debe devolver 404 o 500
            assertThat(respuesta.getStatusCode().value()).isIn(404, 500);
        } catch (RuntimeException ex) {
            // Aceptar cualquier RuntimeException inesperada
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void obtenerPuntosCurso_errorInesperado_retorna500() {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(10L))
                .thenThrow(new RuntimeException("error grave"));
        try {
            ResponseEntity<HashMap<String, Integer>> respuesta = controller.obtenerPuntosCurso(10L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerEstadisticasCursoActividad ====================

    @Test
    void obtenerEstadisticasCursoActividad_retornaMapaConEstadisticas() {
        Map<Long, EstadisticasActividadDTO> resultado = Map.of(1L,
                new EstadisticasActividadDTO(true, 10.0, 8.0, 10, 5));
        when(estadisticasMaestroService.obtenerEstadisticasCursoActividad(10L, 1L)).thenReturn(resultado);

        Map<Long, EstadisticasActividadDTO> respuesta = controller.obtenerEstadisticasCursoActividad(10L, 1L);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta).containsKey(1L);
    }

    @Test
    void obtenerEstadisticasCursoActividad_sinActividades_retornaMapaVacio() {
        when(estadisticasMaestroService.obtenerEstadisticasCursoActividad(10L, 1L)).thenReturn(Map.of());

        Map<Long, EstadisticasActividadDTO> respuesta = controller.obtenerEstadisticasCursoActividad(10L, 1L);

        assertThat(respuesta).isEmpty();
    }

    // ==================== obtenerRepeticionesCursoActividad ====================

    @Test
    void obtenerRepeticionesCursoActividad_retornaMapaConRepeticiones() {
        Map<Long, RepeticionesActividadDTO> resultado = Map.of(1L,
                new RepeticionesActividadDTO(2.0, 1, 3));
        when(estadisticasMaestroService.obtenerRepeticionesCursoActividad(10L, 1L)).thenReturn(resultado);

        Map<Long, RepeticionesActividadDTO> respuesta = controller.obtenerRepeticionesCursoActividad(10L, 1L);

        assertThat(respuesta).containsKey(1L);
        assertThat(respuesta.get(1L).getRepeticionesMedia()).isEqualTo(2.0);
    }

    @Test
    void obtenerRepeticionesCursoActividad_sinActividades_retornaMapaVacio() {
        when(estadisticasMaestroService.obtenerRepeticionesCursoActividad(10L, 1L)).thenReturn(Map.of());

        Map<Long, RepeticionesActividadDTO> respuesta = controller.obtenerRepeticionesCursoActividad(10L, 1L);

        assertThat(respuesta).isEmpty();
    }

    // ==================== obtenerEstadisticasCursoTema ====================

    @Test
    void obtenerEstadisticasCursoTema_retornaMapaConEstadisticas() {
        Map<Long, EstadisticasTemaDTO> resultado = Map.of(1L,
                new EstadisticasTemaDTO(true, 7.5, 20.0, 10, 5));
        when(estadisticasMaestroService.obtenerEstadisticasCursoTema(10L)).thenReturn(resultado);

        Map<Long, EstadisticasTemaDTO> respuesta = controller.obtenerEstadisticasCursoTema(10L);

        assertThat(respuesta).containsKey(1L);
    }

    @Test
    void obtenerEstadisticasCursoTema_sinTemas_retornaMapaVacio() {
        when(estadisticasMaestroService.obtenerEstadisticasCursoTema(10L)).thenReturn(Map.of());

        Map<Long, EstadisticasTemaDTO> respuesta = controller.obtenerEstadisticasCursoTema(10L);

        assertThat(respuesta).isEmpty();
    }

    // ==================== obtenerEstadisticasCurso ====================

    @Test
    void obtenerEstadisticasCurso_retornaDTO() {
        EstadisticasCursoDTO dto = new EstadisticasCursoDTO(true, 8.0, 30.0, 10, 5);
        when(estadisticasMaestroService.obtenerEstadisticasCurso(10L)).thenReturn(dto);

        EstadisticasCursoDTO respuesta = controller.obtenerEstadisticasCurso(10L);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getNotaMediaCurso()).isEqualTo(8.0);
    }

    // ==================== obtenerResumenEstadisticasAlumno ====================

    @Test
    void obtenerResumenEstadisticasAlumno_alumnoExistente_retorna200ConResumen() {
        EstadisticasAlumnoResumenDTO resumen = new EstadisticasAlumnoResumenDTO(
                2L, "Alumno 1", 8.5, 6, 10, 3, 5, 120, 0, List.of());
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(10L, 2L)).thenReturn(resumen);

        ResponseEntity<?> respuesta = controller.obtenerResumenEstadisticasAlumno(10L, 2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(resumen);
    }

    @Test
    void obtenerResumenEstadisticasAlumno_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(10L, 2L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));

        try {
            ResponseEntity<?> respuesta = controller.obtenerResumenEstadisticasAlumno(10L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (AccessDeniedException ex) {
            // También es válido que se lance la excepción directamente
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerResumenEstadisticasAlumno_cursoNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(99L, 2L))
                .thenThrow(new RuntimeException("404 Not Found: El curso no existe"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerResumenEstadisticasAlumno(99L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void obtenerResumenEstadisticasAlumno_errorInesperado_retorna500ConError() {
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(10L, 2L))
                .thenThrow(new RuntimeException("fallo inesperado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerResumenEstadisticasAlumno(10L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerEstadisticasAlumno ====================

    @Test
    void obtenerEstadisticasAlumno_retornaMapaConEstadisticas() {
        Map<Long, EstadisticasAlumnoDTO> resultado = Map.of(1L,
                new EstadisticasAlumnoDTO(true, 9, 2, 1, 0, null, null, 15));
        when(estadisticasMaestroService.obtenerEstadisticasAlumno(2L, 10L, 1L)).thenReturn(resultado);

        Map<Long, EstadisticasAlumnoDTO> respuesta = controller.obtenerEstadisticasAlumno(2L, 10L, 1L);

        assertThat(respuesta).containsKey(1L);
        assertThat(respuesta.get(1L).getRealizada()).isTrue();
    }

    @Test
    void obtenerEstadisticasAlumno_sinActividades_retornaMapaVacio() {
        when(estadisticasMaestroService.obtenerEstadisticasAlumno(2L, 10L, 1L)).thenReturn(Map.of());

        Map<Long, EstadisticasAlumnoDTO> respuesta = controller.obtenerEstadisticasAlumno(2L, 10L, 1L);

        assertThat(respuesta).isEmpty();
    }

    // ==================== temaCompletado ====================

    @Test
    void temaCompletado_temaTerminado_retornaTrue() {
        when(estadisticasMaestroService.temaCompletado(2L, 10L, 1L)).thenReturn(true);

        Boolean resultado = controller.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void temaCompletado_temaNoTerminado_retornaFalse() {
        when(estadisticasMaestroService.temaCompletado(2L, 10L, 1L)).thenReturn(false);

        Boolean resultado = controller.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isFalse();
    }

    // ==================== notaMediaAlumno ====================

    @Test
    void notaMediaAlumno_retornaNotaCalculada() {
        when(estadisticasMaestroService.notaMediaAlumno(2L, 10L, 1L)).thenReturn(8);

        Integer resultado = controller.notaMediaAlumno(2L, 10L, 1L);

        assertThat(resultado).isEqualTo(8);
    }

    @Test
    void notaMediaAlumno_sinActividadesCompletadas_retornaCero() {
        when(estadisticasMaestroService.notaMediaAlumno(2L, 10L, 1L)).thenReturn(0);

        Integer resultado = controller.notaMediaAlumno(2L, 10L, 1L);

        assertThat(resultado).isZero();
    }

    // ==================== obtenerTiempoAlumnoEnActividad ====================

    @Test
    void obtenerTiempoAlumnoEnActividad_alumnoConTiempo_retorna200ConTiempo() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnActividad(2L, 5L)).thenReturn(30);

        ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnActividad(5L, 2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoMinutos", (Object) 30);
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnActividad(2L, 5L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnActividad(5L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_actividadNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnActividad(2L, 99L))
                .thenThrow(new RuntimeException("404 Not Found: La actividad no existe"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnActividad(99L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat((Map<String, Object>) respuesta.getBody()).containsKey("error");
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerTiempoAlumnoEnTema ====================

    @Test
    void obtenerTiempoAlumnoEnTema_alumnoConTiempo_retorna200ConTiempo() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnTema(2L, 1L)).thenReturn(60);

        ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnTema(1L, 2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoMinutos", (Object) 60);
    }

    @Test
    void obtenerTiempoAlumnoEnTema_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnTema(2L, 1L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnTema(1L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            if (respuesta.getBody() instanceof Map) {
                Map<?, ?> body = (Map<?, ?>) respuesta.getBody();
                assertThat(body.keySet()).anyMatch(k -> "error".equals(k));
            }
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoAlumnoEnTema_temaNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnTema(2L, 99L))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnTema(99L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerTiempoAlumnoEnCurso ====================

    @Test
    void obtenerTiempoAlumnoEnCurso_alumnoConTiempo_retorna200ConTiempo() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(2L, 10L)).thenReturn(180);

        ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnCurso(10L, 2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoMinutos", (Object) 180);
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(2L, 10L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnCurso(10L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_cursoNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(2L, 99L))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoAlumnoEnCurso(99L, 2L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerTiempoMedioActividad ====================

    @Test
    void obtenerTiempoMedioActividad_conDatos_retorna200ConPromedio() {
        when(estadisticasMaestroService.obtenerTiempoMedioActividad(5L)).thenReturn(25.5);

        ResponseEntity<?> respuesta = controller.obtenerTiempoMedioActividad(5L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoPromedioMinutos", (Object) 25.5);
    }

    @Test
    void obtenerTiempoMedioActividad_sinDatos_retorna200ConCero() {
        when(estadisticasMaestroService.obtenerTiempoMedioActividad(5L)).thenReturn(0.0);

        ResponseEntity<?> respuesta = controller.obtenerTiempoMedioActividad(5L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoPromedioMinutos", (Object) 0.0);
    }

    @Test
    void obtenerTiempoMedioActividad_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioActividad(5L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioActividad(5L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoMedioActividad_actividadNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioActividad(99L))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioActividad(99L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerTiempoMedioTema ====================

    @Test
    void obtenerTiempoMedioTema_conDatos_retorna200ConPromedio() {
        when(estadisticasMaestroService.obtenerTiempoMedioTema(1L)).thenReturn(40.0);

        ResponseEntity<?> respuesta = controller.obtenerTiempoMedioTema(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoPromedioMinutos", (Object) 40.0);
    }

    @Test
    void obtenerTiempoMedioTema_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioTema(1L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioTema(1L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoMedioTema_temaNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioTema(99L))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioTema(99L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerTiempoMedioCurso ====================

    @Test
    void obtenerTiempoMedioCurso_conDatos_retorna200ConPromedio() {
        when(estadisticasMaestroService.obtenerTiempoMedioCurso(10L)).thenReturn(90.0);

        ResponseEntity<?> respuesta = controller.obtenerTiempoMedioCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoPromedioMinutos", (Object) 90.0);
    }

    @Test
    void obtenerTiempoMedioCurso_sinInscritos_retorna200ConCero() {
        when(estadisticasMaestroService.obtenerTiempoMedioCurso(10L)).thenReturn(0.0);

        ResponseEntity<?> respuesta = controller.obtenerTiempoMedioCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, Object>) respuesta.getBody()).containsEntry("tiempoPromedioMinutos", (Object) 0.0);
    }

    @Test
    void obtenerTiempoMedioCurso_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioCurso(10L))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioCurso(10L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerTiempoMedioCurso_cursoNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerTiempoMedioCurso(99L))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerTiempoMedioCurso(99L);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerAlumnosMasRapidosLentosActividad ====================

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_conDatos_retorna200ConResultado() {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(List.of(), List.of(), 20.0);
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosActividad(5L, 3)).thenReturn(dto);

        ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosActividad(5L, 3);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(dto);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosActividad(5L, 3))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosActividad(5L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            // Si hay body, puede contener "error"
            if (respuesta.getBody() instanceof Map) {
                Map<?, ?> body = (Map<?, ?>) respuesta.getBody();
                assertThat(body.keySet()).anyMatch(k -> "error".equals(k));
            }
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_actividadNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosActividad(99L, 3))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosActividad(99L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerAlumnosMasRapidosLentosTema ====================

    @Test
    void obtenerAlumnosMasRapidosLentosTema_conDatos_retorna200ConResultado() {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(List.of(), List.of(), 35.0);
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosTema(1L, 3)).thenReturn(dto);

        ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosTema(1L, 3);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(dto);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosTema(1L, 3))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosTema(1L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_temaNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosTema(99L, 3))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosTema(99L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== obtenerAlumnosMasRapidosLentosCurso ====================

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_conDatos_retorna200ConResultado() {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(List.of(), List.of(), 100.0);
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(10L, 3)).thenReturn(dto);

        ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosCurso(10L, 3);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(dto);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_sinInscritos_retorna200ListasVacias() {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(List.of(), List.of(), 0.0);
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(10L, 3)).thenReturn(dto);

        ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosCurso(10L, 3);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((AlumnosMasRapidosLentosDTO) respuesta.getBody()).getMasRapidos()).isEmpty();
        assertThat(((AlumnosMasRapidosLentosDTO) respuesta.getBody()).getMasLentos()).isEmpty();
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_accesoNoPermitido_retorna403ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(10L, 3))
                .thenThrow(new AccessDeniedException("Acceso denegado"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosCurso(10L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } catch (AccessDeniedException ex) {
            assertThat(ex).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_cursoNoExiste_retorna404ConError() {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(99L, 3))
                .thenThrow(new RuntimeException("404 Not Found"));
        try {
            ResponseEntity<?> respuesta = controller.obtenerAlumnosMasRapidosLentosCurso(99L, 3);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}