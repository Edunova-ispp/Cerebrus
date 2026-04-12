package com.cerebrus.suscripcion.dto;

import java.time.LocalDate;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SuscripcionDTO {

    private final Long id;
    private final Integer numMaestros;
    private final Integer numAlumnos;
    private final Double precio;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final boolean activa;
    private final EstadoPagoSuscripcion estadoPago;
}
