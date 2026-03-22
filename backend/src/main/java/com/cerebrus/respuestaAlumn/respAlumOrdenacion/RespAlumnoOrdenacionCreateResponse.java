package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionDTO;

public class RespAlumnoOrdenacionCreateResponse {

    private RespAlumnoOrdenacionDTO respAlumnoOrdenacion;
    private String comentario;

    public RespAlumnoOrdenacionCreateResponse() {
    }

    public RespAlumnoOrdenacionCreateResponse(RespAlumnoOrdenacionDTO respAlumnoOrdenacion, String comentario) {
        this.respAlumnoOrdenacion = respAlumnoOrdenacion;
        this.comentario = comentario;
    }

    public RespAlumnoOrdenacionDTO getRespAlumnoOrdenacion() {
        return respAlumnoOrdenacion;
    }

    public void setRespAlumnoOrdenacion(RespAlumnoOrdenacionDTO respAlumnoOrdenacion) {
        this.respAlumnoOrdenacion = respAlumnoOrdenacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
