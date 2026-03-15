package com.cerebrus.respuestaMaestro.dto;

import com.cerebrus.respuestaMaestro.RespuestaMaestro;

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

    public static RespuestaDTO fromEntity(RespuestaMaestro respuesta) {
        return new RespuestaDTO(
            respuesta.getId(),
            respuesta.getRespuesta()
        );
    }
}
