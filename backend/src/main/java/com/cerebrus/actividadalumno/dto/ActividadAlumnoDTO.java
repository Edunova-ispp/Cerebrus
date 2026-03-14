package com.cerebrus.actividadalumno.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActividadAlumnoDTO {

    private Long id;
    
    private Integer tiempo;

    private Integer puntuacion;

    private LocalDateTime inicio;

    private LocalDateTime acabada;

    private Integer nota;

    private Integer numAbandonos;

    private Long alumnoId;

    private Long actividadId;

    public ActividadAlumnoDTO(Long id, Integer tiempo, Integer puntuacion, 
        LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos, Long alumnoId, Long actividadId) {
        this.id = id;
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.inicio = inicio;
        this.acabada = acabada;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }

    public ActividadAlumnoDTO(Integer tiempo, Integer puntuacion, Integer nota, Integer numAbandonos, Long alumnoId, Long actividadId) {
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }
}
