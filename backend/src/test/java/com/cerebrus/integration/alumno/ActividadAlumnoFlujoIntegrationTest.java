package com.cerebrus.integration.alumno;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoController;
import com.cerebrus.actividadAlumn.ActividadAlumnoService;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ActividadAlumnoFlujoIntegrationTest {

    @Mock
    private ActividadAlumnoService actividadAlumnoService;

    @InjectMocks
    private ActividadAlumnoController actividadAlumnoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(actividadAlumnoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void ensureActividadAlumno_devuelveUnoCuandoExiste() throws Exception {
        when(actividadAlumnoService.existeActAlumnoPorActIdYCurrentUserId(6004L)).thenReturn(1);

        mockMvc.perform(get("/api/actividades-alumno/ensure/6004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void abandonarActividadAlumno_devuelveEstadoActualizado() throws Exception {
        ActividadAlumno aa = actividadAlumno(8010L, 6, 2, LocalDateTime.of(2026, 3, 1, 12, 0), null);
        when(actividadAlumnoService.abandonarActAlumnoPorId(8010L)).thenReturn(aa);

        mockMvc.perform(post("/api/actividades-alumno/8010/abandon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8010))
                .andExpect(jsonPath("$.nota").value(6))
                .andExpect(jsonPath("$.numAbandonos").value(2));
    }

    @Test
    void corregirAutomaticamente_conRespuestas_devuelveActividadCorregida() throws Exception {
        ActividadAlumno aa = actividadAlumno(8011L, 10, 0,
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 1, 10, 4));

        when(actividadAlumnoService.corregirActAlumnoAutomaticamente(eq(8011L), eq(List.of(9001L, 9002L))))
                .thenReturn(aa);

        mockMvc.perform(put("/api/actividades-alumno/corregir-automaticamente/8011")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(9001L, 9002L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8011))
                .andExpect(jsonPath("$.nota").value(10))
                .andExpect(jsonPath("$.tiempo").value(4));
    }

    @Test
    void corregirAutomaticamente_sinBody_permiteRecopilarRespuestasEnServicio() throws Exception {
        ActividadAlumno aa = actividadAlumno(8012L, 8, 0,
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 1, 9, 6));

        when(actividadAlumnoService.corregirActAlumnoAutomaticamente(eq(8012L), isNull()))
                .thenReturn(aa);

        mockMvc.perform(put("/api/actividades-alumno/corregir-automaticamente/8012")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8012))
                .andExpect(jsonPath("$.nota").value(8))
                .andExpect(jsonPath("$.tiempo").value(6));
    }

    private static ActividadAlumno actividadAlumno(Long id, Integer nota, Integer numAbandonos,
            LocalDateTime inicio, LocalDateTime fin) {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setId(id);
        aa.setPuntuacion(nota);
        aa.setNota(nota);
        aa.setNumAbandonos(numAbandonos);
        aa.setFechaInicio(inicio);
        aa.setFechaFin(fin == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : fin);
        return aa;
    }
}
