package com.cerebrus.integration.profesor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.puntoImagen.PuntoImagenController;
import com.cerebrus.puntoImagen.PuntoImagenService;

@ExtendWith(MockitoExtension.class)
class PuntoImagenControllerIntegrationTest {

    @Mock
    private PuntoImagenService puntoImagenService;

    @InjectMocks
    private PuntoImagenController puntoImagenController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(puntoImagenController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void encontrarPuntoImagenPorId_exists_devuelve200() throws Exception {
        PuntoImagen punto = new PuntoImagen();
        punto.setId(20L);
        punto.setRespuesta("HTML");
        punto.setPixelX(100);
        punto.setPixelY(200);

        when(puntoImagenService.encontrarPuntoImagenPorId(20L)).thenReturn(punto);

        mockMvc.perform(get("/api/puntos-imagen/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.respuesta").value("HTML"))
                .andExpect(jsonPath("$.pixelX").value(100))
                .andExpect(jsonPath("$.pixelY").value(200));
    }

    @Test
    void encontrarPuntoImagenPorId_notFound_devuelve404() throws Exception {
        when(puntoImagenService.encontrarPuntoImagenPorId(999L))
                .thenThrow(new ResourceNotFoundException("PuntoImagen", "id", 999L));

        mockMvc.perform(get("/api/puntos-imagen/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarPuntoImagenPorId_accessDenied_devuelve403() throws Exception {
        when(puntoImagenService.encontrarPuntoImagenPorId(20L))
                .thenThrow(new AccessDeniedException("Solo un usuario logueado como alumno o maestro puede obtener los puntos de la imagen"));

        mockMvc.perform(get("/api/puntos-imagen/20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void eliminarPuntoImagenPorId_success_devuelve204() throws Exception {
        doNothing().when(puntoImagenService).eliminarPuntoImagenPorId(20L);

        mockMvc.perform(delete("/api/puntos-imagen/20"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarPuntoImagenPorId_notFound_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("PuntoImagen", "id", 999L))
                .when(puntoImagenService).eliminarPuntoImagenPorId(999L);

        mockMvc.perform(delete("/api/puntos-imagen/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void eliminarPuntoImagenPorId_soloMaestroAutorizado_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo un maestro puede eliminar puntos de la imagen"))
                .when(puntoImagenService).eliminarPuntoImagenPorId(20L);

        mockMvc.perform(delete("/api/puntos-imagen/20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void eliminarPuntoImagenPorId_sinAutenticacion_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Usuario no autenticado"))
                .when(puntoImagenService).eliminarPuntoImagenPorId(20L);

        mockMvc.perform(delete("/api/puntos-imagen/20"))
                .andExpect(status().isForbidden());
    }

    @Test
    void encontrarPuntoImagenPorId_multipleRequests_respectContentType() throws Exception {
        PuntoImagen punto = new PuntoImagen();
        punto.setId(21L);
        punto.setRespuesta("CSS");
        punto.setPixelX(150);
        punto.setPixelY(250);

        when(puntoImagenService.encontrarPuntoImagenPorId(21L)).thenReturn(punto);

        mockMvc.perform(get("/api/puntos-imagen/21")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").value("CSS"))
                .andExpect(jsonPath("$.pixelX").value(150));
    }

    @Test
    void eliminarPuntoImagenPorId_serverError_devuelve500() throws Exception {
        doThrow(new RuntimeException("Unexpected error during deletion"))
                .when(puntoImagenService).eliminarPuntoImagenPorId(20L);

        mockMvc.perform(delete("/api/puntos-imagen/20"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarPuntoImagenPorId_variousPixelCoordinates_devuelve200() throws Exception {
        PuntoImagen punto = new PuntoImagen();
        punto.setId(22L);
        punto.setRespuesta("JavaScript");
        punto.setPixelX(0);
        punto.setPixelY(0);

        when(puntoImagenService.encontrarPuntoImagenPorId(22L)).thenReturn(punto);

        mockMvc.perform(get("/api/puntos-imagen/22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pixelX").value(0))
                .andExpect(jsonPath("$.pixelY").value(0));
    }
}
