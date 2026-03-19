package com.cerebrus.estadisticas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasCursoDTO {

    public EstadisticasCursoDTO(Boolean cursoCompletadoPorTodos, Double notaMediaCurso,
            Double tiempoMedioCurso, Integer notaMaximaCurso, Integer notaMinimaCurso) {
        this.cursoCompletadoPorTodos = cursoCompletadoPorTodos;
        this.notaMediaCurso = notaMediaCurso;
        this.tiempoMedioCurso = tiempoMedioCurso;
        this.notaMaximaCurso = notaMaximaCurso;
        this.notaMinimaCurso = notaMinimaCurso;
    }

    private Boolean cursoCompletadoPorTodos;
    private Double notaMediaCurso;
    private Double tiempoMedioCurso;
    private Integer notaMaximaCurso;
    private Integer notaMinimaCurso;
}
