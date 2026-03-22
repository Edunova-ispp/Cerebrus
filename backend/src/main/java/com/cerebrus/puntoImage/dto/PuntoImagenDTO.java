package com.cerebrus.puntoImage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PuntoImagenDTO {

    private final Long id;
    private final String respuesta;
    private final Integer pixelX;
    private final Integer pixelY;

    public PuntoImagenDTO(Long id, String respuesta, Integer pixelX, Integer pixelY) {
        this.id = id;
        this.respuesta = respuesta;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

}
