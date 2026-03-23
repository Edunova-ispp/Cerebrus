package com.cerebrus.respuestaAlumno.respAlumGeneral;

import java.util.List;

public class EvaluacionActividadAbiertaResponse {
    private Integer notaFinal;
    private Integer puntuacionFinal;
    private List<RespAlumnoAbiertaResponse> detallesPreguntas;

    public EvaluacionActividadAbiertaResponse(Integer notaFinal, Integer puntuacionFinal, List<RespAlumnoAbiertaResponse> detallesPreguntas) {
        this.notaFinal = notaFinal;
        this.puntuacionFinal = puntuacionFinal;
        this.detallesPreguntas = detallesPreguntas;
    }

    public Integer getNotaFinal() { return notaFinal; }
    public void setNotaFinal(Integer notaFinal) { this.notaFinal = notaFinal; }

    public Integer getPuntuacionFinal() { return puntuacionFinal; }
    public void setPuntuacionFinal(Integer puntuacionFinal) { this.puntuacionFinal = puntuacionFinal; }

    public List<RespAlumnoAbiertaResponse> getDetallesPreguntas() { return detallesPreguntas; }
    public void setDetallesPreguntas(List<RespAlumnoAbiertaResponse> detallesPreguntas) { this.detallesPreguntas = detallesPreguntas; }
}