package com.cerebrus.respuestaAlumn.respAlumGeneral.dto;

/**
 * Flat DTO returned after creating a RespAlumnoGeneral.
 * Does NOT expose entity objects to avoid lazy-proxy serialization issues.
 */
public class RespAlumnoGeneralCreateResponse {
    private Long id;
    private Boolean correcta;
    private String comentario;

    public RespAlumnoGeneralCreateResponse() {
    }

    public RespAlumnoGeneralCreateResponse(Long id, Boolean correcta, String comentario) {
        this.id = id;
        this.correcta = correcta;
        this.comentario = comentario;
    }

    public Boolean getCorrecta() {
        return correcta;
    }

    public void setCorrecta(Boolean correcta) {
        this.correcta = correcta;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
