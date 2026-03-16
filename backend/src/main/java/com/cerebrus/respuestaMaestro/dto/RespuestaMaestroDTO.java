package com.cerebrus.respuestaMaestro.dto;

public class RespuestaMaestroDTO {

    private final Long id;
    private final String respuesta;
    private final Boolean correcta;

    public RespuestaMaestroDTO(Long id, String respuesta, Boolean correcta) {
        this.id = id;
        this.respuesta = respuesta;
        this.correcta = correcta;
    }

    public Long getId() {
        return id;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public Boolean getCorrecta() {
        return correcta;
    }
}
