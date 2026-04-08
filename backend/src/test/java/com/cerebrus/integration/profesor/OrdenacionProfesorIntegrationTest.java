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

import java.util.List;
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

import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionController;
import com.cerebrus.actividad.ordenacion.OrdenacionService;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.tema.Tema;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrdenacionProfesorIntegrationTest {

    @Mock
    private OrdenacionService ordenacionService;

    @InjectMocks
    private OrdenacionController ordenacionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(ordenacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearOrdenacion_ok_devuelve201ConId() throws Exception {
        Ordenacion ordenacion = new Ordenacion();
        ordenacion.setId(66L);

        when(ordenacionService.crearActOrdenacion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ordenacion);

        Map<String, Object> body = Map.of(
                "titulo", "Ordenar planetas",
                "descripcion", "desc",
                "puntuacion", 10,
                "imagen", "img.png",
                "respVisible", true,
                "comentariosRespVisible", "coment",
                "posicion", 1,
                "tema", Map.of("id", 1L),
                "valores", List.of("Mercurio", "Venus", "Tierra")
        );

        mockMvc.perform(post("/api/ordenaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(66));
    }

    @Test
    void actualizarOrdenacion_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo el maestro del curso puede actualizar esta actividad"))
                .when(ordenacionService)
                .actualizarActOrdenacion(eq(7L), any(), any(), any(), any(), any(), any(), any(), any(), any());

        Map<String, Object> body = Map.of(
                "titulo", "Ordenar edit",
                "descripcion", "desc",
                "puntuacion", 8,
                "imagen", "img2.png",
                "respVisible", false,
                "comentariosRespVisible", "coment",
                "posicion", 1,
                "tema", Map.of("id", 1L),
                "valores", List.of("A", "B")
        );

        mockMvc.perform(put("/api/ordenaciones/update/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el maestro del curso puede actualizar esta actividad"));
    }

    @Test
    void eliminarOrdenacion_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo el maestro del curso puede eliminar esta actividad"))
                .when(ordenacionService).eliminarActOrdenacionPorId(7L);

        mockMvc.perform(delete("/api/ordenaciones/delete/7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el maestro del curso puede eliminar esta actividad"));
    }
}
