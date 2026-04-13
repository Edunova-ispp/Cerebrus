package com.cerebrus.pregunta.dto;

import java.util.List;

import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaMaestro.dto.RespuestaDTO;

public class PreguntaDTO {

    private final Long id;
    private final String pregunta;
    private final String imagen;
    private final List<RespuestaDTO> respuestas;
    private final Integer numRespuestasCorrectas;

    public PreguntaDTO(Long id, String pregunta, String imagen, List<RespuestaDTO> respuestas) {
        this(id, pregunta, imagen, respuestas, null);
    }

    public PreguntaDTO(Long id, String pregunta, String imagen, List<RespuestaDTO> respuestas, Integer numRespuestasCorrectas) {
        this.id = id;
        this.pregunta = pregunta;
        this.imagen = imagen;
        this.respuestas = respuestas;
        this.numRespuestasCorrectas = numRespuestasCorrectas;
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

    public List<RespuestaDTO> getRespuestas() {
        return respuestas;
    }

    public Integer getNumRespuestasCorrectas() {
        return numRespuestasCorrectas;
    }

    public static PreguntaDTO fromEntity(Pregunta pregunta) {
        List<RespuestaDTO> respuestasDTO = pregunta.getRespuestasMaestro().stream()
            .map(RespuestaDTO::fromEntity)
            .toList();

        return new PreguntaDTO(
            pregunta.getId(),
            pregunta.getPregunta(),
            pregunta.getImagen(),
            respuestasDTO,
            (int) pregunta.getRespuestasMaestro().stream().filter(r -> Boolean.TRUE.equals(r.getCorrecta())).count()
        );
    }
}
