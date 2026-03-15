package com.cerebrus.respuestaAlumno.respAlumPuntoImagen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespAlumnoPuntoImagenDTO {

    private Long id;
    private String respuesta;
    private Integer pixelX;
    private Integer pixelY;
    private Long marcarImagenId;
    private Long actividadAlumnoId;

    public RespAlumnoPuntoImagenDTO(Long id, String respuesta, Integer pixelX, Integer pixelY, Long marcarImagenId, Long actividadAlumnoId) {
        this.id = id;
        this.respuesta = respuesta;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.marcarImagenId = marcarImagenId;
        this.actividadAlumnoId = actividadAlumnoId;
    }

}
