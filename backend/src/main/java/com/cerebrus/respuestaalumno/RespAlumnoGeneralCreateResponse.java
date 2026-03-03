package com.cerebrus.respuestaalumno;

/**
 * Flat DTO returned after creating a RespAlumnoGeneral.
 * Does NOT expose entity objects to avoid lazy-proxy serialization issues.
 */
public class RespAlumnoGeneralCreateResponse {

    private Boolean correcta;
    private String comentario;

    public RespAlumnoGeneralCreateResponse() {
    }

    public RespAlumnoGeneralCreateResponse(Boolean correcta, String comentario) {
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
}
