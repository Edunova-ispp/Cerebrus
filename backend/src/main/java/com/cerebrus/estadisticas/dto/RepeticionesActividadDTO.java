package com.cerebrus.estadisticas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeticionesActividadDTO {

    private Double repeticionesMedia;
    private Integer repeticionesMinima;
    private Integer repeticionesMaxima;

    public RepeticionesActividadDTO() {
    }

    public RepeticionesActividadDTO(Double repeticionesMedia, Integer repeticionesMinima, Integer repeticionesMaxima) {
        this.repeticionesMedia = repeticionesMedia;
        this.repeticionesMinima = repeticionesMinima;
        this.repeticionesMaxima = repeticionesMaxima;
    }
}
