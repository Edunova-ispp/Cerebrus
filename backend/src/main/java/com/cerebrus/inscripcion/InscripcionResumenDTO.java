package com.cerebrus.inscripcion;

public class InscripcionResumenDTO {
    private Long cursoId;
    private Integer puntos;

    public InscripcionResumenDTO(Long cursoId, Integer puntos) {
        this.cursoId = cursoId;
        this.puntos = puntos;
    }

    public Long getCursoId() { return cursoId; }
    public Integer getPuntos() { return puntos; }
}
