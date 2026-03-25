package com.cerebrus.respuestaAlumn.respAlumPuntoImagen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespAlumnoPuntoImagenDTO {

    private Long id;
    private String respuesta;
    
    @NotNull(message = "El ID del punto de imagen es obligatorio")
    private Long puntoImagenId;

    @NotNull(message = "El ID de la actividad del alumno es obligatorio")
    private Long actividadAlumnoId;

    public RespAlumnoPuntoImagenDTO(Long id, String respuesta, Long puntoImagenId, Long actividadAlumnoId) {
        this.id = id;
        this.respuesta = respuesta;
        this.puntoImagenId = puntoImagenId;
        this.actividadAlumnoId = actividadAlumnoId;
    }

}
