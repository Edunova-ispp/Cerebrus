package com.cerebrus.actividadalumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActividadAlumnoDTO {

    private Integer tiempo;

    private Integer puntuacion;

    private Integer nota;

    private Integer numAbandonos;

    private Long alumnoId;

    private Long actividadId;

    public ActividadAlumnoDTO(Integer tiempo, Integer puntuacion, Integer nota, Integer numAbandonos, Long alumnoId, Long actividadId) {
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }

}
