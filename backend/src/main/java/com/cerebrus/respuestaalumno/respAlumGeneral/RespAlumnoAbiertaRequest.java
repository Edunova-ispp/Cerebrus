package com.cerebrus.respuestaAlumno.respAlumGeneral;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class RespAlumnoAbiertaRequest {

    @NotNull
    private Long actividadAlumnoId;

    @NotNull
    private Long preguntaId;

    @NotBlank(message = "La respuesta no puede estar vacía")
    private String respuestaAlumno;

    public RespAlumnoAbiertaRequest() {
    }

    public RespAlumnoAbiertaRequest(Long actividadAlumnoId, Long preguntaId, String respuestaAlumno) {
        this.actividadAlumnoId = actividadAlumnoId;
        this.preguntaId = preguntaId;
        this.respuestaAlumno = respuestaAlumno;
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

    public String getRespuestaAlumno() {
        return respuestaAlumno;
    }

    public void setRespuestaAlumno(String respuestaAlumno) {
        this.respuestaAlumno = respuestaAlumno;
    }
}
