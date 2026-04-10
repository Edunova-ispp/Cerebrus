package com.cerebrus.suscripcion.pago.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SesionPagoDTO {

    private String transaccionId;
    private String urlPago;
    
}
