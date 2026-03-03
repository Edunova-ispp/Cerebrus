package com.cerebrus.respuesta;

public class RespuestaDTO {

    private final Long id;
    private final String respuesta;

    public RespuestaDTO(Long id, String respuesta) {
        this.id = id;
        this.respuesta = respuesta;
    }

    public Long getId() {
        return id;
    }

    public String getRespuesta() {
        return respuesta;
    }
}
