package com.cerebrus.suscripcion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SuscripcionRequest {
    private Integer numMaestros;
    private Integer numAlumnos;
    private Integer numMeses;
}

