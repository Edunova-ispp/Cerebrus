package com.cerebrus.respuestaAlumno.respAlumPuntoImagen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespAlumnoPuntoImagenDTO {

    private Long id;

    @NotBlank(message = "La respuesta es obligatoria")
    private String respuesta;
    
    @NotNull(message = "El ID del punto de imagen es obligatorio")
    private Long puntoImagenId;

    @NotNull(message = "El ID de la actividad del alumno es obligatorio")
    private Long actividadAlumnoId;

    public RespAlumnoPuntoImagenDTO() {
    }

    public RespAlumnoPuntoImagenDTO(Long id, String respuesta, Long puntoImagenId, Long actividadAlumnoId) {
        this.id = id;
        this.respuesta = respuesta;
        this.puntoImagenId = puntoImagenId;
        this.actividadAlumnoId = actividadAlumnoId;
    }

}
