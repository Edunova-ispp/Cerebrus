package com.cerebrus.suscripcionTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.cerebrus.suscripcion.pago.MockPagoService;

class MockPagoServiceTest {

    @Test
    void crearSesionDePago_generaTransaccionYUrlConDatos() {
        MockPagoService service = new MockPagoService();
        ReflectionTestUtils.setField(service, "frontendUrl", "http://frontend.local");

        var sesion = service.crearSesionDePago(77L, 49.99);

        assertThat(sesion.getTransaccionId()).startsWith("txn_mock_");
        assertThat(sesion.getUrlPago()).contains("http://frontend.local/simulador-pago");
        assertThat(sesion.getUrlPago()).contains("txn=");
        assertThat(sesion.getUrlPago()).contains("susId=77");
    }
}
