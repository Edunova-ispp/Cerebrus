package com.cerebrus.respuestaalumno;

/**
 * Flat DTO returned after creating a RespAlumnoGeneral.
 * Does NOT expose entity objects to avoid lazy-proxy serialization issues.
 */
public class RespAlumnoGeneralCreateResponse {

    private boolean correcta;
    private String comentario;

    public RespAlumnoGeneralCreateResponse() {
    }

    public RespAlumnoGeneralCreateResponse(boolean correcta, String comentario) {
        this.correcta = correcta;
        this.comentario = comentario;
    }

    public boolean isCorrecta() {
        return correcta;
    }

    public void setCorrecta(boolean correcta) {
        this.correcta = correcta;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
