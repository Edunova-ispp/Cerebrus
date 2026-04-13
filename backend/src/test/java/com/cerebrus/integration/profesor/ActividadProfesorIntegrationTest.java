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

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadController;
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.tema.Tema;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ActividadProfesorIntegrationTest {

    @Mock
    private ActividadService actividadService;

    @InjectMocks
    private ActividadController actividadController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(actividadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearTeoria_ok_devuelve201() throws Exception {
        Actividad actividad = buildActividad(901L, "Teoria 1", "desc", "img.png", 7, 1L);
        when(actividadService.crearActTeoria(any(), any(), any(), any(), any())).thenReturn(actividad);

        Map<String, Object> body = Map.of(
                "titulo", "Teoria 1",
                "descripcion", "desc",
                "imagen", "img.png",
                "temaId", 1
        );

        mockMvc.perform(post("/api/actividades/teoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(901));
    }

    @Test
    void crearTeoria_payloadInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "",
                "descripcion", "desc",
                "imagen", "img.png",
                "temaId", 1
        );

        mockMvc.perform(post("/api/actividades/teoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void actualizarTeoria_ok_devuelve200() throws Exception {
        Actividad actividad = buildActividad(902L, "Teoria 2 edit", "desc", "img2.png", 8, 1L);
        when(actividadService.actualizarActTeoria(eq(902L), any(), any(), any(), any())).thenReturn(actividad);

        Map<String, Object> body = Map.of(
                "titulo", "Teoria 2 edit",
                "descripcion", "desc",
                "imagen", "img2.png",
                "temaId", 1
        );

        mockMvc.perform(put("/api/actividades/teoria/902")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(902));
    }

    @Test
    void actualizarTeoria_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No puedes editar actividades de cursos que no son tuyos"))
                .when(actividadService).actualizarActTeoria(eq(902L), any(), any(), any(), any());

        Map<String, Object> body = Map.of(
                "titulo", "Teoria 2 edit",
                "descripcion", "desc",
                "imagen", "img2.png",
                "temaId", 1
        );

        mockMvc.perform(put("/api/actividades/teoria/902")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No puedes editar actividades de cursos que no son tuyos"));
    }

    @Test
    void eliminarTeoria_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No puedes eliminar actividades de cursos que no son tuyos"))
                .when(actividadService).eliminarActTeoriaPorId(902L);

        mockMvc.perform(delete("/api/actividades/delete/902"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No puedes eliminar actividades de cursos que no son tuyos"));
    }

    @Test
    void eliminarTeoria_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/actividades/delete/902"))
                .andExpect(status().isNoContent());
    }

    private Actividad buildActividad(Long id, String titulo, String descripcion, String imagen, int posicion, Long temaId) {
        Actividad actividad = new Actividad() { };
        actividad.setId(id);
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setImagen(imagen);
        actividad.setPosicion(posicion);
        Tema tema = new Tema();
        tema.setId(temaId);
        actividad.setTema(tema);
        return actividad;
    }
}
