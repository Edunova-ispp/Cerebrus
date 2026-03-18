package com.cerebrus.estadisticas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasActividadDTO {

    public EstadisticasActividadDTO(Boolean actividadCompletadaPorTodos, Double tiempoMedioActividad,
            Double notaMediaActividad, Integer notaMaximaActividad, Integer notaMinimaActividad) {
        this.actividadCompletadaPorTodos = actividadCompletadaPorTodos;
        this.tiempoMedioActividad = tiempoMedioActividad;
        this.notaMediaActividad = notaMediaActividad;
        this.notaMaximaActividad = notaMaximaActividad;
        this.notaMinimaActividad = notaMinimaActividad;
    }
    
    private Boolean actividadCompletadaPorTodos;
    private Double tiempoMedioActividad;
    private Double notaMediaActividad;
    private Integer notaMaximaActividad;
    private Integer notaMinimaActividad;

}
