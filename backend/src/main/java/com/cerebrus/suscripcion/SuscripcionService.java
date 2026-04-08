package com.cerebrus.suscripcion;

import java.util.List;

import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;

public interface SuscripcionService {

    List<SuscripcionDTO> obtenerSuscripcionesOrganizacion(Long organizacionId);

    SuscripcionDTO obtenerSuscripcionOrganizacion(Long organizacionId, Long suscripcionId);
    
    SuscripcionDTO obtenerSuscripcionActivaOrganizacion(Long organizacionId);

    PlanPreciosDTO obtenerPlanPrecios();

    Suscripcion crearSuscripcion(Long organizacionId, SuscripcionRequest request);

}
