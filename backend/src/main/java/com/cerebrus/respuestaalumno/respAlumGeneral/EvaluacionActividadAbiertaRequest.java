package com.cerebrus.respuestaAlumno.respAlumGeneral;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.LinkedHashMap;

public class EvaluacionActividadAbiertaRequest {

    @NotNull
    private Long actividadAlumnoId;

    @NotEmpty(message = "Debe enviar al menos una respuesta")
    private LinkedHashMap<Long, String> respuestasAlumno;

    public EvaluacionActividadAbiertaRequest() {
    }

    public EvaluacionActividadAbiertaRequest(Long actividadAlumnoId, LinkedHashMap<Long, String> respuestasAlumno) {
        this.actividadAlumnoId = actividadAlumnoId;
        this.respuestasAlumno = respuestasAlumno;
    }

    public Long getActividadAlumnoId() {
        return actividadAlumnoId;
    }

    public void setActividadAlumnoId(Long actividadAlumnoId) {
        this.actividadAlumnoId = actividadAlumnoId;
    }

    public LinkedHashMap<Long, String> getRespuestasAlumno() {
        return respuestasAlumno;
    }

    public void setRespuestasAlumno(LinkedHashMap<Long, String> respuestasAlumno) {
        this.respuestasAlumno = respuestasAlumno;
    }
}