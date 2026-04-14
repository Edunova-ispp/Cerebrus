package com.cerebrus.integration.profesor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.EstadisticasMaestroController;
import com.cerebrus.estadisticas.EstadisticasMaestroService;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.IntentoActividadDTO;
import com.cerebrus.estadisticas.dto.IntentoActividadDetalleDTO;
import com.cerebrus.estadisticas.dto.IntentoDetalleRespuestaDTO;
import com.cerebrus.exceptions.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class EstadisticasProfesorIntegrationTest {

    @Mock
    private EstadisticasMaestroService estadisticasMaestroService;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private EstadisticasMaestroController estadisticasMaestroController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(estadisticasMaestroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerEstadisticasCurso_ok_devuelve200() throws Exception {
        EstadisticasCursoDTO dto = new EstadisticasCursoDTO(true, 7.6, 32.1, 10, 4);
        when(estadisticasMaestroService.obtenerEstadisticasCurso(4001L)).thenReturn(dto);

        mockMvc.perform(get("/api/estadisticas/cursos/4001/estadisiticas-curso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursoCompletadoPorTodos").value(true))
                .andExpect(jsonPath("$.notaMediaCurso").value(7.6))
                .andExpect(jsonPath("$.tiempoMedioCurso").value(32.1));
    }

    @Test
    void obtenerActividadesCompletadas_ok_devuelveMapa() throws Exception {
        Curso curso = new Curso();
        curso.setId(4001L);

        when(cursoRepository.findById(4001L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso))
                .thenReturn(Map.of("Harry Potter", 5L));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/actividades-completadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['Harry Potter']").value(5));
    }

    @Test
    void obtenerActividadesCompletadas_cursoNoExiste_devuelve404() throws Exception {
        when(cursoRepository.findById(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/estadisticas/cursos/9999/actividades-completadas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void obtenerActividadesCompletadas_noPropietario_devuelve403() throws Exception {
        Curso curso = new Curso();
        curso.setId(4001L);

        when(cursoRepository.findById(4001L)).thenReturn(Optional.of(curso));
        when(estadisticasMaestroService.numActividadesRealizadasPorAlumno(curso))
                .thenThrow(new AccessDeniedException("Solo un maestro propietario"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/actividades-completadas"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Solo un maestro propietario"));
    }

    @Test
    void obtenerPuntosCurso_noPropietario_devuelve403() throws Exception {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(4001L))
                .thenThrow(new AccessDeniedException("No autorizado"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/puntos"))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPuntosCurso_cursoNoExiste_devuelve404() throws Exception {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(9999L))
                .thenThrow(new RuntimeException("404 Not Found: curso"));

        mockMvc.perform(get("/api/estadisticas/cursos/9999/puntos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerEstadisticasActividadTema_ok_devuelve200() throws Exception {
        when(estadisticasMaestroService.obtenerEstadisticasCursoActividad(4001L, 5001L))
                .thenReturn(Map.of(6001L, new EstadisticasActividadDTO(true, 20.0, 8.0, 10, 4)));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/temas/5001/estadisticas-actividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.6001.actividadCompletadaPorTodos").value(true))
                .andExpect(jsonPath("$.6001.notaMediaActividad").value(8.0));
    }

    @Test
    void obtenerAlumnosRapidosLentos_limiteCero_edgeCase_devuelve200() throws Exception {
        AlumnosMasRapidosLentosDTO dto = new AlumnosMasRapidosLentosDTO(List.of(), List.of(), 0.0);
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(eq(4001L), eq(0))).thenReturn(dto);

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumnos-rapidos-lentos?limite=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.masRapidos").isArray())
                .andExpect(jsonPath("$.masLentos").isArray())
                .andExpect(jsonPath("$.tiempoPromedio").value(0.0));
    }

    @Test
    void obtenerAlumnosRapidosLentos_noPropietario_devuelve403() throws Exception {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(4001L, 3))
                .thenThrow(new AccessDeniedException("No autorizado"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumnos-rapidos-lentos"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void obtenerTiempoAlumnoCurso_ok_devuelveTiempo() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(2101L, 4001L)).thenReturn(123);

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumno/2101/tiempo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiempoMinutos").value(123));
    }

    @Test
    void obtenerResumenEstadisticasAlumno_noExiste_devuelve404() throws Exception {
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(4001L, 999L))
                .thenThrow(new RuntimeException("404 Not Found: alumno"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumnos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void obtenerResumenEstadisticasAlumno_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerResumenEstadisticasAlumno(4001L, 2101L))
                .thenThrow(new RuntimeException("error interno"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumnos/2101"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("error interno"));
    }

    @Test
    void obtenerPuntosCurso_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(4001L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/puntos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerTiempoAlumnoCurso_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnCurso(2101L, 4001L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/cursos/4001/alumno/2101/tiempo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerTiempoAlumnoEnTema_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnTema(2101L, 5001L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/temas/5001/alumno/2101/tiempo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoAlumnoEnActividad(2101L, 6001L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/actividades/6001/alumno/2101/tiempo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerTiempoMedioActividad_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoMedioActividad(5L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/actividades/5/tiempo-promedio"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerTiempoMedioTema_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoMedioTema(1L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/temas/1/tiempo-promedio"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerTiempoMedioCurso_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerTiempoMedioCurso(10L))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/cursos/10/tiempo-promedio"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosActividad(5L, 3))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/actividades/5/alumnos-rapidos-lentos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosTema(1L, 3))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/temas/1/alumnos-rapidos-lentos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.obtenerAlumnosMasRapidosLentosCurso(10L, 3))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/estadisticas/cursos/10/alumnos-rapidos-lentos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }

    @Test
    void getMethodName_devuelveCadenaVacia() {
        assertThat(estadisticasMaestroController.getMethodName("cualquiera")).isEmpty();
    }

    @Test
    void obtenerDetalleIntento_ok_devuelve200() throws Exception {
        IntentoActividadDetalleDTO detalle = new IntentoActividadDetalleDTO(
                900L,
                10L,
                2L,
                30L,
                "Actividad",
                "GeneralTest",
                "imagen.png",
                100,
                null,
                null,
                10,
                0,
                8,
                8,
                0,
                List.of(new IntentoDetalleRespuestaDTO(1L, "GENERAL", "Pregunta", "Respuesta", true, 0)));

        when(estadisticasMaestroService.obtenerDetalleIntento(10L, 2L, 30L, 900L)).thenReturn(detalle);

        mockMvc.perform(get("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentoId").value(900L))
                .andExpect(jsonPath("$.actividadTipo").value("GeneralTest"));
    }

    @Test
    void obtenerDetalleIntento_accesoNoPermitido_devuelve403() throws Exception {
        when(estadisticasMaestroService.obtenerDetalleIntento(10L, 2L, 30L, 900L))
                .thenThrow(new AccessDeniedException("No autorizado"));

        mockMvc.perform(get("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void obtenerDetalleIntento_noEncontrado_devuelve404() throws Exception {
        when(estadisticasMaestroService.obtenerDetalleIntento(10L, 2L, 30L, 900L))
                .thenThrow(new RuntimeException("404 Not Found: intento"));

        mockMvc.perform(get("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("404 Not Found: intento"));
    }

    @Test
    void actualizarPuntuacionIntento_ok_devuelve200() throws Exception {
        IntentoActividadDTO actualizado = new IntentoActividadDTO(900L, null, null, 50, 5, 10, 0, 0);
        when(estadisticasMaestroService.actualizarPuntuacionIntento(10L, 2L, 30L, 900L, 50))
                .thenReturn(actualizado);

        mockMvc.perform(put("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900/puntuacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntuacion").value(50))
                .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    void actualizarPuntuacionIntento_puntuacionInvalida_devuelve400() throws Exception {
        when(estadisticasMaestroService.actualizarPuntuacionIntento(10L, 2L, 30L, 900L, null))
                .thenThrow(new IllegalArgumentException("La puntuación es obligatoria."));

        mockMvc.perform(put("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900/puntuacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La puntuación es obligatoria."));
    }

    @Test
    void actualizarPuntuacionIntento_accesoNoPermitido_devuelve403() throws Exception {
        when(estadisticasMaestroService.actualizarPuntuacionIntento(10L, 2L, 30L, 900L, 50))
                .thenThrow(new AccessDeniedException("No autorizado"));

        mockMvc.perform(put("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900/puntuacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":50}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void actualizarPuntuacionIntento_noEncontrado_devuelve404() throws Exception {
        when(estadisticasMaestroService.actualizarPuntuacionIntento(10L, 2L, 30L, 900L, 50))
                .thenThrow(new RuntimeException("404 Not Found: intento"));

        mockMvc.perform(put("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900/puntuacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":50}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("404 Not Found: intento"));
    }

    @Test
    void actualizarPuntuacionIntento_errorInesperado_devuelve500() throws Exception {
        when(estadisticasMaestroService.actualizarPuntuacionIntento(10L, 2L, 30L, 900L, 50))
                .thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(put("/api/estadisticas/cursos/10/alumnos/2/actividades/30/intentos/900/puntuacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":50}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("fallo inesperado"));
    }
}
