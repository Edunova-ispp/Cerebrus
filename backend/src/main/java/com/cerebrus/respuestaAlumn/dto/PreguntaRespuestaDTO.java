package com.cerebrus.respuestaAlumn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa una pregunta con su respuesta del alumno y respuesta correcta
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaRespuestaDTO {
    
    /**
     * ID de la pregunta
     */
    private Long preguntaId;
    
    /**
     * Texto/contenido de la pregunta
     */
    private String pregunta;
    
    /**
     * URL de la imagen de la pregunta (si aplica)
     */
    private String imagenPregunta;
    
    /**
     * Respuesta del alumno (puede ser null si no está permitido verla)
     */
    private Object respuestaAlumno;
    
    /**
     * Respuesta correcta (puede ser null si no está permitido verla)
     */
    private Object respuestaCorrecta;
    
    /**
     * Si la respuesta del alumno es correcta
     */
    private Boolean respuestaAlumnoCorrecta;
}
