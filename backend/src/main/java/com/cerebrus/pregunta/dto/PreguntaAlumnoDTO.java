package com.cerebrus.pregunta.dto;

// DTO creado para obtener la actividad sin las repuestas de Maestro para Alumno
public class PreguntaAlumnoDTO {

    private final Long id;
    private final String pregunta;
    private final String imagen;

    public PreguntaAlumnoDTO(Long id, String pregunta, String imagen) {
        this.id = id;
        this.pregunta = pregunta;
        this.imagen = imagen;
    }

    public Long getId() {
        return id;
    }

    public String getPregunta() {
        return pregunta;
    }

    public String getImagen() {
        return imagen;
    }
}
