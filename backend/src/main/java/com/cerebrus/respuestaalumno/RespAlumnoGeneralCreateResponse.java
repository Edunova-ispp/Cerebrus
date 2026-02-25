package com.cerebrus.respuestaalumno;

public class RespAlumnoGeneralCreateResponse {

    private RespAlumnoGeneral respAlumnoGeneral;
    private String comentario;

    public RespAlumnoGeneralCreateResponse() {
    }

    public RespAlumnoGeneralCreateResponse(RespAlumnoGeneral respAlumnoGeneral, String comentario) {
        this.respAlumnoGeneral = respAlumnoGeneral;
        this.comentario = comentario;
    }

    public RespAlumnoGeneral getRespAlumnoGeneral() {
        return respAlumnoGeneral;
    }

    public void setRespAlumnoGeneral(RespAlumnoGeneral respAlumnoGeneral) {
        this.respAlumnoGeneral = respAlumnoGeneral;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
