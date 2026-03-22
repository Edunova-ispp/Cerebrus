package com.cerebrus.pregunta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PreguntaRequest {

    @NotBlank(message = "El enunciado de la pregunta es obligatorio")
    private String pregunta;

    private String imagen;

    @NotNull(message = "La actividad es obligatoria")
    private Long actividadId;

    public PreguntaRequest() {}

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
}
