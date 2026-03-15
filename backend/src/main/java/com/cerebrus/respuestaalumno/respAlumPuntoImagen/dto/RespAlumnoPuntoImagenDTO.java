package com.cerebrus.respuestaAlumno.respAlumPuntoImagen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespAlumnoPuntoImagenDTO {

    private Long id;
    private String respuesta;
    private Long puntoImagenId;
    private Long actividadAlumnoId;

    public RespAlumnoPuntoImagenDTO(Long id, String respuesta, Long puntoImagenId, Long actividadAlumnoId) {
        this.id = id;
        this.respuesta = respuesta;
        this.puntoImagenId = puntoImagenId;
        this.actividadAlumnoId = actividadAlumnoId;
    }

}
