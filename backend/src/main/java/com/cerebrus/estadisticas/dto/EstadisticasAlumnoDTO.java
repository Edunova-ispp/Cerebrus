package com.cerebrus.estadisticas.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasAlumnoDTO {
    public EstadisticasAlumnoDTO(Boolean realizada, Integer nota, Integer numRepeticiones, Integer numFallos, Integer numAbandonos, LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer tiempo) {
        this.realizada = realizada;
        this.nota = nota;
        this.numRepeticiones = numRepeticiones;
        this.numFallos = numFallos;
        this.numAbandonos = numAbandonos;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tiempo = tiempo;
    }

    private Boolean realizada;
    private Integer nota;
    private Integer numRepeticiones;
    private Integer numFallos;
    private Integer numAbandonos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempo;
    
}
