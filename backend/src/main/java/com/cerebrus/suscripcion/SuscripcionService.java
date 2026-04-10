package com.cerebrus.suscripcion;

import java.util.List;

import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.pago.dto.PagoResponseDTO;
import com.cerebrus.suscripcion.pago.dto.ResumenCompraDTO;

public interface SuscripcionService {

    List<SuscripcionDTO> obtenerSuscripcionesOrganizacion(Long organizacionId);

    SuscripcionDTO obtenerSuscripcionOrganizacion(Long organizacionId, Long suscripcionId);
    
    SuscripcionDTO obtenerSuscripcionActivaOrganizacion(Long organizacionId);

    PlanPreciosDTO obtenerPlanPrecios();
    
    ResumenCompraDTO resumirSuscripcionAComprar(Long organizacionId, SuscripcionRequest request);

    PagoResponseDTO crearSuscripcion(Long organizacionId, SuscripcionRequest request);

    void confirmarPagoExitoso(String transaccionId);

}
