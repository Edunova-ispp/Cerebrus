package com.cerebrus.respuestaalumno;

public class RespAlumnoOrdenacionCreateResponse {

    private RespAlumnoOrdenacion respAlumnoOrdenacion;
    private String comentario;

    public RespAlumnoOrdenacionCreateResponse() {
    }

    public RespAlumnoOrdenacionCreateResponse(RespAlumnoOrdenacion respAlumnoOrdenacion, String comentario) {
        this.respAlumnoOrdenacion = respAlumnoOrdenacion;
        this.comentario = comentario;
    }

    public RespAlumnoOrdenacion getRespAlumnoOrdenacion() {
        return respAlumnoOrdenacion;
    }

    public void setRespAlumnoOrdenacion(RespAlumnoOrdenacion respAlumnoOrdenacion) {
        this.respAlumnoOrdenacion = respAlumnoOrdenacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
