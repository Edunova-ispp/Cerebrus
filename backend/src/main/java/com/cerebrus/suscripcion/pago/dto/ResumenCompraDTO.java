package com.cerebrus.suscripcion.pago.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCompraDTO {

    private String nombreUsuario;
    private String nombreCentro;
    private String correoElectronico;
    private String nombreComprador;
    private String primerApellidoComprador;
    private String segundoApellidoComprador;
    private Integer numMaestros;
    private Integer numAlumnos;
    private Integer meses;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double precioTotal;
}
