package com.cerebrus.respuestaAlumn.respAlumGeneral.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class EvaluacionActividadAbiertaResponse {
    private Integer notaFinal;
    private Integer puntuacionFinal;
    private List<RespAlumnoAbiertaResponse> detallesPreguntas;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean iaNoDisponible;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String iaMensaje;

    public EvaluacionActividadAbiertaResponse(Integer notaFinal, Integer puntuacionFinal, List<RespAlumnoAbiertaResponse> detallesPreguntas) {
        this.notaFinal = notaFinal;
        this.puntuacionFinal = puntuacionFinal;
        this.detallesPreguntas = detallesPreguntas;
        this.iaNoDisponible = null;
        this.iaMensaje = null;
    }

    public EvaluacionActividadAbiertaResponse(Integer notaFinal, Integer puntuacionFinal, List<RespAlumnoAbiertaResponse> detallesPreguntas,
            Boolean iaNoDisponible, String iaMensaje) {
        this.notaFinal = notaFinal;
        this.puntuacionFinal = puntuacionFinal;
        this.detallesPreguntas = detallesPreguntas;
        this.iaNoDisponible = iaNoDisponible;
        this.iaMensaje = iaMensaje;
    }

    public Integer getNotaFinal() { return notaFinal; }
    public void setNotaFinal(Integer notaFinal) { this.notaFinal = notaFinal; }

    public Integer getPuntuacionFinal() { return puntuacionFinal; }
    public void setPuntuacionFinal(Integer puntuacionFinal) { this.puntuacionFinal = puntuacionFinal; }

    public List<RespAlumnoAbiertaResponse> getDetallesPreguntas() { return detallesPreguntas; }
    public void setDetallesPreguntas(List<RespAlumnoAbiertaResponse> detallesPreguntas) { this.detallesPreguntas = detallesPreguntas; }

    public Boolean getIaNoDisponible() { return iaNoDisponible; }
    public void setIaNoDisponible(Boolean iaNoDisponible) { this.iaNoDisponible = iaNoDisponible; }

    public String getIaMensaje() { return iaMensaje; }
    public void setIaMensaje(String iaMensaje) { this.iaMensaje = iaMensaje; }
}