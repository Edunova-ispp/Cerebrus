package com.cerebrus.integration.profesor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.comun.enumerados.TipoAct;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.iaconnection.IaConnectionController;
import com.cerebrus.iaconnection.IaConnectionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class IaProfesorIntegrationTest {

    @Mock
    private IaConnectionService iaConnectionService;

    @InjectMocks
    private IaConnectionController iaConnectionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(iaConnectionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void generarMock_tipoInvalido_devuelve400() throws Exception {
        Map<String, Object> body = Map.of(
                "tipoActividad", "INVALIDA",
                "prompt", "crear actividad"
        );

        mockMvc.perform(post("/api/iaconnection/mock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generarMock_ok_devuelveJson() throws Exception {
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEST), any())).thenReturn("{\"ok\":true}");

        Map<String, Object> body = Map.of(
                "tipoActividad", "TEST",
                "prompt", "crear test"
        );

        mockMvc.perform(post("/api/iaconnection/mock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"ok\":true}"));
    }

    @Test
    void generarActividad_ok_devuelveJson() throws Exception {
        when(iaConnectionService.generarActividad(eq(TipoAct.TEORIA), any())).thenReturn("{\"tipo\":\"TEORIA\"}");

        Map<String, Object> body = Map.of(
                "tipoActividad", "TEORIA",
                "descripcion", "crear teoria"
        );

        mockMvc.perform(post("/api/iaconnection/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"tipo\":\"TEORIA\"}"));
    }

    @Test
    void generarActividad_tipoInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "tipoActividad", "INVALIDA",
                "prompt", "crear actividad"
        );

        mockMvc.perform(post("/api/iaconnection/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }
}
