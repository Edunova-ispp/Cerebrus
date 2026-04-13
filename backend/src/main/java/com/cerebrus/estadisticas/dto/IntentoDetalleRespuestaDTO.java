package com.cerebrus.estadisticas.dto;

public class IntentoDetalleRespuestaDTO {

    private Long respuestaId;
    private String tipoRespuesta;
    private String enunciado;
    private String respuestaAlumno;
    private Boolean correcta;
    private Integer numFallos;

    public IntentoDetalleRespuestaDTO() {}

    public IntentoDetalleRespuestaDTO(Long respuestaId, String tipoRespuesta, String enunciado,
            String respuestaAlumno, Boolean correcta, Integer numFallos) {
        this.respuestaId = respuestaId;
        this.tipoRespuesta = tipoRespuesta;
        this.enunciado = enunciado;
        this.respuestaAlumno = respuestaAlumno;
        this.correcta = correcta;
        this.numFallos = numFallos;
    }

    public Long getRespuestaId() {
        return respuestaId;
    }

    public void setRespuestaId(Long respuestaId) {
        this.respuestaId = respuestaId;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getRespuestaAlumno() {
        return respuestaAlumno;
    }

    public void setRespuestaAlumno(String respuestaAlumno) {
        this.respuestaAlumno = respuestaAlumno;
    }

    public Boolean getCorrecta() {
        return correcta;
    }

    public void setCorrecta(Boolean correcta) {
        this.correcta = correcta;
    }

    public Integer getNumFallos() {
        return numFallos;
    }

    public void setNumFallos(Integer numFallos) {
        this.numFallos = numFallos;
    }
}
