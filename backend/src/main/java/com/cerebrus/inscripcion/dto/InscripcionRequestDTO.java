package com.cerebrus.inscripcion.dto;

import java.util.List;

/**
 * DTO para solicitar la inscripción de múltiples alumnos en un curso
 */
public class InscripcionRequestDTO {
    
    private List<Long> alumnoIds;
    
    // Constructores
    public InscripcionRequestDTO() {
    }
    
    public InscripcionRequestDTO(List<Long> alumnoIds) {
        this.alumnoIds = alumnoIds;
    }
    
    // Getters y Setters
    public List<Long> getAlumnoIds() {
        return alumnoIds;
    }
    
    public void setAlumnoIds(List<Long> alumnoIds) {
        this.alumnoIds = alumnoIds;
    }
    
    @Override
    public String toString() {
        return "InscripcionRequestDTO{" +
                "alumnoIds=" + alumnoIds +
                '}';
    }
}
