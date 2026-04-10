package com.cerebrus.integration.profesor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.actividad.tablero.TableroController;
import com.cerebrus.actividad.tablero.TableroService;
import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TableroProfesorIntegrationTest {

    @Mock
    private TableroService tableroService;

    @InjectMocks
    private TableroController tableroController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tableroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearTablero_ok_devuelve201() throws Exception {
                TableroDTO dto = new TableroDTO(1L, "Tablero", "desc", true, 1, 10, true, 1L, java.util.List.of(), true, false, true, false);
        when(tableroService.crearActTablero(any())).thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Tablero",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "tamano", true,
                "preguntasYRespuestas", preguntas(8)
        );

        mockMvc.perform(post("/api/tableros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void crearTablero_edgeTamanoIncorrecto_devuelve400() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "Tablero",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "tamano", true,
                "preguntasYRespuestas", preguntas(15)
        );

        mockMvc.perform(post("/api/tableros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarTablero_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No tienes permiso para actualizar este tablero porque no eres el maestro del curso"))
                .when(tableroService).actualizarActTablero(eq(3L), any());

        Map<String, Object> body = Map.of(
                "titulo", "Tablero edit",
                "descripcion", "desc",
                "puntuacion", 9,
                "temaId", 1,
                "respVisible", true,
                "tamano", false,
                "preguntasYRespuestas", preguntas(15)
        );

        mockMvc.perform(put("/api/tableros/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para actualizar este tablero porque no eres el maestro del curso"));
    }

    @Test
    void eliminarTablero_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No tienes permiso para eliminar este tablero porque no eres el maestro del curso"))
                .when(tableroService).eliminarActTableroPorId(3L);

        mockMvc.perform(delete("/api/tableros/3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para eliminar este tablero porque no eres el maestro del curso"));
    }

    private Map<String, String> preguntas(int count) {
        Map<String, String> preguntas = new LinkedHashMap<>();
        for (int i = 1; i <= count; i++) {
            preguntas.put("Pregunta " + i, "Respuesta " + i);
        }
        return preguntas;
    }
}
