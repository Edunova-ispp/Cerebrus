package com.cerebrus.respuestaAlumn.respAlumGeneral;

public class RespAlumnoAbiertaResponse {

    private Long respAlumnoId;
    private Integer puntuacion;
    private String comentarios;
    private Boolean respVisible;
    private String comentariosRespVisible;

    public RespAlumnoAbiertaResponse(Long respAlumnoId, Integer puntuacion, String comentarios, 
                                    Boolean respVisible, String comentariosRespVisible) {
        this.respAlumnoId = respAlumnoId;
        this.puntuacion = puntuacion;
        this.comentarios = comentarios;
        this.respVisible = respVisible;
        this.comentariosRespVisible = comentariosRespVisible;
    }

    public Long getRespAlumnoId() {
        return respAlumnoId;
    }

    public void setRespAlumnoId(Long respAlumnoId) {
        this.respAlumnoId = respAlumnoId;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Boolean getRespVisible() {
        return respVisible;
    }

    public void setRespVisible(Boolean respVisible) {
        this.respVisible = respVisible;
    }

    public String getComentariosRespVisible() {
        return comentariosRespVisible;
    }

    public void setComentariosRespVisible(String comentariosRespVisible) {
        this.comentariosRespVisible = comentariosRespVisible;
    }
}
