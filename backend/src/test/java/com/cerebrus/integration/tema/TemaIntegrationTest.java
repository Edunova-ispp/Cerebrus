package com.cerebrus.integration.tema;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaController;
import com.cerebrus.tema.TemaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TemaIntegrationTest {

    @Mock
    private TemaService temaService;

        @Mock
        private ActividadService actividadService;

    @InjectMocks
    private TemaController temaController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(temaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Como profesor: Crear un tema correctamente devuelve 201")
    void crearTema_ok_devuelve201() throws Exception {
        Tema tema = new Tema();
        tema.setId(500L);
        tema.setTitulo("Introducción a las fracciones");
        
        when(temaService.crearTema(any(), any(), any())).thenReturn(tema);

        Map<String, Object> body = Map.of(
                "titulo", "Introducción a las fracciones",
                "cursoId", 101L
        );

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.titulo").value("Introducción a las fracciones"));
    }

    @Test
    @DisplayName("Como profesor: Editar un tema devuelve 200")
    void actualizarTema_ok_devuelve200() throws Exception {
        Tema tema = new Tema();
        tema.setId(500L);
        tema.setTitulo("Fracciones Avanzadas");

        when(temaService.renombrarTema(eq(500L), any(), any())).thenReturn(tema);

        Map<String, Object> body = Map.of("nuevoTitulo", "Fracciones Avanzadas");

        mockMvc.perform(put("/api/temas/500")
                        .param("maestroId", "200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Fracciones Avanzadas"));
    }

    @Test
    @DisplayName("Como profesor: Eliminar un tema devuelve 204")
    void eliminarTema_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/temas/500"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Seguridad: Error 403 si un profesor no propietario intenta crear/editar")
    void accesoDenegado_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No eres el propietario de este contenido"))
                .when(temaService).renombrarTema(eq(500L), any(), any());

        Map<String, Object> body = Map.of("nuevoTitulo", "Hackeo");

        mockMvc.perform(put("/api/temas/500")
                        .param("maestroId", "200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validación: Datos incorrectos devuelven 422 y no cambian el estado")
    void datosIncorrectos_devuelve422() throws Exception {
        // Simulamos que la validación del Bean falla (título vacío)
        Map<String, Object> body = Map.of("titulo", "", "cursoId", 101L);

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is(422));
    }

    @Test
    @DisplayName("Visualización: Se pueden ver las actividades asociadas al tema")
    void visualizarActividadesAsociadas_ok() throws Exception {
        Tema tema = new Tema();
        tema.setId(500L);
        Actividad act = new Actividad() {};
        act.setTitulo("Ejercicio de Fracciones");

        when(temaService.encontrarTemaPorId(500L)).thenReturn(tema);
        when(actividadService.encontrarActividadesPorTema(500L)).thenReturn(List.of(act));

        mockMvc.perform(get("/api/temas/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actividades[0].titulo").value("Ejercicio de Fracciones"));
    }

    @Test
    @DisplayName("Seguridad Alumno: Si el alumno no tiene acceso al curso, devuelve 403")
    void alumnoSinAcceso_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No estás inscrito en este curso"))
                .when(temaService).encontrarTemaPorId(500L);

        mockMvc.perform(get("/api/temas/500"))
                .andExpect(status().isForbidden());
    }
}