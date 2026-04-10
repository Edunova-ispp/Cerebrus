package com.cerebrus.suscripcion.pago;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cerebrus.suscripcion.pago.dto.SesionPagoDTO;

@Service
public class MockPagoService {

    @Value("${cerebrus.app.frontend.url}")
    private String frontendUrl;

    public SesionPagoDTO crearSesionDePago(Long suscripcionId, Double costoSuscripcion) {
        String transaccionMockId = "txn_mock_" + UUID.randomUUID().toString();
        String urlPagoFalsa = frontendUrl + "/simulador-pago?txn=" + transaccionMockId + "&susId=" + suscripcionId;
        
        return new SesionPagoDTO(transaccionMockId, urlPagoFalsa);
    }
}
