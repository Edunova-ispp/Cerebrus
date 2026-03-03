package com.cerebrus.respuestaalumno;

import jakarta.validation.constraints.NotNull;

public class RespAlumnoGeneralRequest {

    @NotNull
    private Long actividadAlumnoId;

    @NotNull
    private Long preguntaId;

    @NotNull
    private Long respuestaId;

    public RespAlumnoGeneralRequest() {
    }

    public Long getActividadAlumnoId() {
        return actividadAlumnoId;
    }

    public void setActividadAlumnoId(Long actividadAlumnoId) {
        this.actividadAlumnoId = actividadAlumnoId;
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public void setPreguntaId(Long preguntaId) {
        this.preguntaId = preguntaId;
    }

    public Long getRespuestaId() {
        return respuestaId;
    }

    public void setRespuestaId(Long respuestaId) {
        this.respuestaId = respuestaId;
    }
}
