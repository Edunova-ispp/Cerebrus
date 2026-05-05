package com.cerebrus.suscripcionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.suscripcion.SuscripcionController;
import com.cerebrus.suscripcion.SuscripcionService;
import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.pago.dto.PagoResponseDTO;
import com.cerebrus.suscripcion.pago.dto.ResumenCompraDTO;

@ExtendWith(MockitoExtension.class)
class SuscripcionControllerTest {

    @Mock
    private SuscripcionService suscripcionService;

    @InjectMocks
    private SuscripcionController controller;

    @Test
    void encontrarSuscripcionesOrganizacion_devuelveOk() {
        SuscripcionDTO dto = new SuscripcionDTO(1L, 2, 3, 50.0, null, null, true, EstadoPagoSuscripcion.PAGADA);
        when(suscripcionService.obtenerSuscripcionesOrganizacion(1001L)).thenReturn(List.of(dto));

        ResponseEntity<List<SuscripcionDTO>> response = controller.encontrarSuscripcionesOrganizacion(1001L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
        verify(suscripcionService).obtenerSuscripcionesOrganizacion(1001L);
    }

    @Test
    void encontrarSuscripcionActivaOrganizacion_devuelveDTO() {
        SuscripcionDTO dto = new SuscripcionDTO(1L, 2, 3, 50.0, null, null, true, EstadoPagoSuscripcion.PAGADA);
        when(suscripcionService.obtenerSuscripcionActivaOrganizacion(1001L)).thenReturn(dto);

        SuscripcionDTO response = controller.encontrarSuscripcionActivaOrganizacion(1001L);

        assertThat(response).isEqualTo(dto);
        verify(suscripcionService).obtenerSuscripcionActivaOrganizacion(1001L);
    }

    @Test
    void encontrarSuscripcionOrganizacion_devuelveDTO() {
        SuscripcionDTO dto = new SuscripcionDTO(2L, 4, 5, 120.0, null, null, false, EstadoPagoSuscripcion.PENDIENTE);
        when(suscripcionService.obtenerSuscripcionOrganizacion(1001L, 2L)).thenReturn(dto);

        ResponseEntity<SuscripcionDTO> response = controller.encontrarSuscripcionOrganizacion(1001L, 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(suscripcionService).obtenerSuscripcionOrganizacion(1001L, 2L);
    }

    @Test
    void encontrarSuscripcionOrganizacion_propagadaExcepcionDelServicio() {
        when(suscripcionService.obtenerSuscripcionOrganizacion(1001L, 2L))
                .thenThrow(new IllegalArgumentException("No se encontró la suscripción de la organización"));

        assertThatThrownBy(() -> controller.encontrarSuscripcionOrganizacion(1001L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontró la suscripción de la organización");
    }

    @Test
    void obtenerPlanPrecios_devuelveOk() {
        PlanPreciosDTO dto = new PlanPreciosDTO(java.util.Map.of("profesor", 20), java.util.Map.of("profesor", 10.0));
        when(suscripcionService.obtenerPlanPrecios()).thenReturn(dto);

        ResponseEntity<PlanPreciosDTO> response = controller.obtenerPlanPrecios();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void resumirSuscripcionAComprar_devuelveOk() {
        SuscripcionRequest request = new SuscripcionRequest(2, 3, 1);
        ResumenCompraDTO dto = new ResumenCompraDTO();
        when(suscripcionService.resumirSuscripcionAComprar(1001L, request)).thenReturn(dto);

        ResponseEntity<ResumenCompraDTO> response = controller.resumirSuscripcionAComprar(1001L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(suscripcionService).resumirSuscripcionAComprar(1001L, request);
    }

    @Test
    void crearSuscripcion_devuelveCreated() {
        SuscripcionRequest request = new SuscripcionRequest(2, 3, 1);
        PagoResponseDTO dto = new PagoResponseDTO("txn-1", "http://front");
        when(suscripcionService.crearSuscripcion(1001L, request)).thenReturn(dto);

        ResponseEntity<PagoResponseDTO> response = controller.crearSuscripcion(1001L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(suscripcionService).crearSuscripcion(1001L, request);
    }

    @Test
    void confirmarPago_devuelveOk_siServicioNoFalla() {
        org.mockito.Mockito.doNothing().when(suscripcionService).confirmarPagoExitoso("txn-1");

        ResponseEntity<String> response = controller.confirmarPago("txn-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Pago confirmado y suscripción activada.");
        verify(suscripcionService).confirmarPagoExitoso("txn-1");
    }

    @Test
    void confirmarPago_devuelveBadRequest_siServicioFalla() {
        doThrow(new IllegalArgumentException("Transacción no encontrada"))
            .when(suscripcionService).confirmarPagoExitoso("txn-1");

        ResponseEntity<String> response = controller.confirmarPago("txn-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Error al confirmar el pago: Transacción no encontrada");
        verify(suscripcionService).confirmarPagoExitoso("txn-1");
    }
}
