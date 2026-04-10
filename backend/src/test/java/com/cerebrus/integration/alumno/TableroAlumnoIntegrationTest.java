package com.cerebrus.integration.alumno;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.cerebrus.actividad.tablero.TableroController;
import com.cerebrus.actividad.tablero.TableroService;
import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.pregunta.dto.PreguntaDTO;

@ExtendWith(MockitoExtension.class)
class TableroAlumnoIntegrationTest {

    @Mock
    private TableroService tableroService;

    @InjectMocks
    private TableroController tableroController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tableroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerTableroAlumno_ok_devuelve200() throws Exception {
        TableroDTO dto = new TableroDTO(
                6006L,
                "Tablero básico",
                "Resuelve las preguntas",
                true,
                1,
                10,
                true,
                5001L,
                List.of(new PreguntaDTO(7001L, "Pregunta 1", null, List.of())),
                true,
                true,
                true,
                true
        );

        when(tableroService.encontrarActTableroPorId(6006L)).thenReturn(dto);

        mockMvc.perform(get("/api/tableros/6006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6006))
                .andExpect(jsonPath("$.titulo").value("Tablero básico"))
                .andExpect(jsonPath("$.tamano").value(true))
                .andExpect(jsonPath("$.preguntas[0].pregunta").value("Pregunta 1"));
    }

    @Test
    void responderPreguntaTablero_correcta_devuelve200YMensaje() throws Exception {
                when(tableroService.crearRespuestaAPreguntaEnActTablero("\"respuesta\"", 6006L, 7001L))
                .thenReturn("Respuesta correcta");

        mockMvc.perform(post("/api/tableros/6006/7001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"respuesta\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Respuesta correcta"));
    }

    @Test
    void responderPreguntaTablero_invalida_devuelve422() throws Exception {
        when(tableroService.crearRespuestaAPreguntaEnActTablero("\"\"", 6006L, 7001L))
                .thenThrow(new IllegalArgumentException("La respuesta no puede estar vacía"));

        mockMvc.perform(post("/api/tableros/6006/7001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"\""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensaje").value("La respuesta no puede estar vacía"));
    }
}
