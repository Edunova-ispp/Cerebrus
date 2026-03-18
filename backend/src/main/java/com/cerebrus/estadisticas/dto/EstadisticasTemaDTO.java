package com.cerebrus.estadisticas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasTemaDTO {

    public EstadisticasTemaDTO(Boolean temaCompletadoPorTodos, Double notaMediaTema,
            Double tiempoMedioTema, Integer notaMaximaTema, Integer notaMinimaTema) {
        this.temaCompletadoPorTodos = temaCompletadoPorTodos;
        this.notaMediaTema = notaMediaTema;
        this.tiempoMedioTema = tiempoMedioTema;
        this.notaMaximaTema = notaMaximaTema;
        this.notaMinimaTema = notaMinimaTema;
    }

    private Boolean temaCompletadoPorTodos;
    private Double notaMediaTema;
    private Double tiempoMedioTema;
    private Integer notaMaximaTema;
    private Integer notaMinimaTema;
}
