package com.cerebrus.respuestaAlumn.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la respuesta del endpoint que obtiene las respuestas del alumno y las respuestas correctas
 * de una actividad completada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespuestasActividadDTO {
    
    /**
     * ID de la actividad
     */
    private Long actividadId;
    
    /**
     * Título de la actividad
     */
    private String actividadTitulo;
    
    /**
     * Lista de objetos que representan cada pregunta con sus respuestas
     */
    private List<PreguntaRespuestaDTO> preguntas;
    
    /**
     * Si la actividad fue completada correctamente
     */
    private Boolean completadaCorrectamente;
    
    /**
     * Nota obtenida en la actividad (0-10)
     */
    private Integer nota;
}
