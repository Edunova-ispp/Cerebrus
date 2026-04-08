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

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenController;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;
import com.cerebrus.tema.Tema;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MarcarImagenProfesorIntegrationTest {

    @Mock
    private MarcarImagenService marcarImagenService;

    @InjectMocks
    private MarcarImagenController marcarImagenController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(marcarImagenController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearMarcarImagen_ok_devuelve201() throws Exception {
        MarcarImagen created = new MarcarImagen();
        created.setId(99L);
        Tema tema = new Tema();
        tema.setId(1L);
        created.setTema(tema);
        created.setPuntosImagen(java.util.List.of());

        MarcarImagenDTO request = new MarcarImagenDTO(
                null,
                "Marcar Imagen",
                "desc",
                10,
                "img.png",
                true,
                "coment",
                1L,
                "mapa.png",
                List.of(new PuntoImagenDTO(null, "A", 10, 20))
        );

        when(marcarImagenService.crearActMarcarImagen(any())).thenReturn(created);

        mockMvc.perform(post("/api/marcar-imagenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void actualizarMarcarImagen_noPropietario_devuelve403() throws Exception {
        MarcarImagenDTO request = new MarcarImagenDTO(
                1L,
                "Marcar Imagen",
                "desc",
                10,
                "img.png",
                true,
                "coment",
                1L,
                "mapa.png",
                List.of(new PuntoImagenDTO(1L, "A", 10, 20))
        );

        doThrow(new AccessDeniedException("Solo el maestro del curso puede actualizar actividades en ese tema"))
                .when(marcarImagenService).actualizarActMarcarImagen(eq(8L), any());

        mockMvc.perform(put("/api/marcar-imagenes/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el maestro del curso puede actualizar actividades en ese tema"));
    }

    @Test
    void eliminarMarcarImagen_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo el maestro del curso puede eliminar esta actividad"))
                .when(marcarImagenService).eliminarActMarcarImagenPorId(8L);

        mockMvc.perform(delete("/api/marcar-imagenes/8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el maestro del curso puede eliminar esta actividad"));
    }

    @Test
    void crearMarcarImagen_payloadInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "imagenAMarcar", "mapa.png",
                "puntosImagen", List.of()
        );

        mockMvc.perform(post("/api/marcar-imagenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }
}
