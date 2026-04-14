package com.cerebrus.integration.suscripcion;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.suscripcion.SuscripcionController;
import com.cerebrus.suscripcion.SuscripcionService;
import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.pago.dto.PagoResponseDTO;
import com.cerebrus.suscripcion.pago.dto.ResumenCompraDTO;

@ExtendWith(MockitoExtension.class)
class SuscripcionControllerIntegrationTest {

    @Mock
    private SuscripcionService suscripcionService;

    @InjectMocks
    private SuscripcionController suscripcionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(suscripcionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void encontrarSuscripcionesOrganizacion_ok_devuelve200() throws Exception {
        SuscripcionDTO dto = new SuscripcionDTO(1L, 2, 3, 120.0, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), true, EstadoPagoSuscripcion.PAGADA);
        when(suscripcionService.obtenerSuscripcionesOrganizacion(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/suscripciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numMaestros").value(2))
                .andExpect(jsonPath("$[0].activa").value(true));
    }

    @Test
    void encontrarSuscripcionesOrganizacion_accessDenied_devuelve403() throws Exception {
        when(suscripcionService.obtenerSuscripcionesOrganizacion(1L))
                .thenThrow(new AccessDeniedException("Solo una organización puede acceder a sus suscripciones"));

        mockMvc.perform(get("/api/suscripciones/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarSuscripcionesOrganizacion_sinSuscripciones_devuelve422() throws Exception {
        when(suscripcionService.obtenerSuscripcionesOrganizacion(1L))
                .thenThrow(new IllegalArgumentException("La organización no tiene suscripciones, contrata una para acceder a las funcionalidades de Cerebrus"));

        mockMvc.perform(get("/api/suscripciones/1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarSuscripcionOrganizacion_ok_devuelve200() throws Exception {
        SuscripcionDTO dto = new SuscripcionDTO(2L, 3, 4, 150.0, LocalDate.now().minusDays(1), LocalDate.now().plusDays(20), true, EstadoPagoSuscripcion.PAGADA);
        when(suscripcionService.obtenerSuscripcionOrganizacion(1L, 2L)).thenReturn(dto);

        mockMvc.perform(get("/api/suscripciones/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.numAlumnos").value(4));
    }

    @Test
    void encontrarSuscripcionOrganizacion_accessDenied_devuelve403() throws Exception {
        when(suscripcionService.obtenerSuscripcionOrganizacion(1L, 2L))
                .thenThrow(new AccessDeniedException("No se puede acceder a una suscripción de una organización diferente a la del usuario logueado"));

        mockMvc.perform(get("/api/suscripciones/1/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarSuscripcionOrganizacion_noEncontrada_devuelve422() throws Exception {
        when(suscripcionService.obtenerSuscripcionOrganizacion(1L, 2L))
                .thenThrow(new IllegalArgumentException("No se encontró la suscripción de la organización"));

        mockMvc.perform(get("/api/suscripciones/1/2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void encontrarSuscripcionActivaOrganizacion_ok_devuelve200() throws Exception {
        SuscripcionDTO dto = new SuscripcionDTO(3L, 3, 5, 130.0, LocalDate.now().minusDays(1), LocalDate.now().plusDays(15), true, EstadoPagoSuscripcion.PAGADA);
        when(suscripcionService.obtenerSuscripcionActivaOrganizacion(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/suscripciones/activa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void encontrarSuscripcionActivaOrganizacion_noActiva_devuelve422() throws Exception {
        when(suscripcionService.obtenerSuscripcionActivaOrganizacion(1L))
                .thenThrow(new IllegalArgumentException("No se encontró una suscripción activa para la organización, contrata una nueva para acceder a las funcionalidades de Cerebrus"));

        mockMvc.perform(get("/api/suscripciones/activa/1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void obtenerPlanPrecios_ok_devuelve200() throws Exception {
        PlanPreciosDTO dto = new PlanPreciosDTO(java.util.Map.of("profesor", 20, "alumno", 50), java.util.Map.of("profesor", 10.0, "alumno", 5.0));
        when(suscripcionService.obtenerPlanPrecios()).thenReturn(dto);

        mockMvc.perform(get("/api/suscripciones/plan-precios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitesBase.profesor").value(20))
                .andExpect(jsonPath("$.preciosBase.profesor").value(10.0));
    }

    @Test
    void obtenerPlanPrecios_accessDenied_devuelve403() throws Exception {
        when(suscripcionService.obtenerPlanPrecios())
                .thenThrow(new AccessDeniedException("Solo una organización puede acceder a los planes de precios"));

        mockMvc.perform(get("/api/suscripciones/plan-precios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void resumirSuscripcionAComprar_ok_devuelve200() throws Exception {
        ResumenCompraDTO dto = new ResumenCompraDTO("org1", "Centro 1", "org1@test.com", "Nombre", "Apellido1", "Apellido2", 2, 3, 4, LocalDate.now(), LocalDate.now().plusMonths(4), 140.0);
                when(suscripcionService.resumirSuscripcionAComprar(eq(1L), any(SuscripcionRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suscripciones/organizacion/1/resumir-suscripcion-a-comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":2,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numMaestros").value(2))
                .andExpect(jsonPath("$.precioTotal").value(140.0));
    }

    @Test
    void resumirSuscripcionAComprar_accessDenied_devuelve403() throws Exception {
                when(suscripcionService.resumirSuscripcionAComprar(eq(1L), any(SuscripcionRequest.class)))
                .thenThrow(new AccessDeniedException("Solo una organización puede crear una suscripción"));

        mockMvc.perform(post("/api/suscripciones/organizacion/1/resumir-suscripcion-a-comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":2,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void resumirSuscripcionAComprar_parametrosInvalidos_devuelve422() throws Exception {
                when(suscripcionService.resumirSuscripcionAComprar(eq(1L), any(SuscripcionRequest.class)))
                .thenThrow(new IllegalArgumentException("El número de profesores, alumnos y meses debe ser mayor a cero"));

        mockMvc.perform(post("/api/suscripciones/organizacion/1/resumir-suscripcion-a-comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":0,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void crearSuscripcion_ok_devuelve201() throws Exception {
        PagoResponseDTO dto = new PagoResponseDTO("txn-200", "https://pago.test/txn-200");
                when(suscripcionService.crearSuscripcion(eq(1L), any(SuscripcionRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/suscripciones/crear-suscripcion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":2,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaccionId").value("txn-200"))
                .andExpect(jsonPath("$.urlPago").value("https://pago.test/txn-200"));
    }

    @Test
    void crearSuscripcion_accessDenied_devuelve403() throws Exception {
                when(suscripcionService.crearSuscripcion(eq(1L), any(SuscripcionRequest.class)))
                .thenThrow(new AccessDeniedException("Solo una organización puede realizar una suscripción"));

        mockMvc.perform(post("/api/suscripciones/crear-suscripcion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":2,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void crearSuscripcion_yaExisteActiva_devuelve422() throws Exception {
                when(suscripcionService.crearSuscripcion(eq(1L), any(SuscripcionRequest.class)))
                .thenThrow(new IllegalArgumentException("La organización ya tiene una suscripción activa."));

        mockMvc.perform(post("/api/suscripciones/crear-suscripcion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numMaestros\":2,\"numAlumnos\":3,\"numMeses\":4}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void confirmarPago_ok_devuelve200() throws Exception {
                doNothing().when(suscripcionService).confirmarPagoExitoso("txn-200");

        mockMvc.perform(post("/api/suscripciones/confirmar-pago/txn-200"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pago confirmado y suscripción activada."));
    }

    @Test
    void confirmarPago_error_devuelve400() throws Exception {
        doThrow(new IllegalArgumentException("Transacción no encontrada: txn-missing"))
                .when(suscripcionService).confirmarPagoExitoso("txn-missing");

        mockMvc.perform(post("/api/suscripciones/confirmar-pago/txn-missing"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al confirmar el pago: Transacción no encontrada: txn-missing"));
    }
}