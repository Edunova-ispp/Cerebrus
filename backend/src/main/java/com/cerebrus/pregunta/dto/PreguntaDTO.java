package com.cerebrus.pregunta.dto;

import java.util.List;

import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaMaestro.dto.RespuestaDTO;

public class PreguntaDTO {

    private final Long id;
    private final String pregunta;
    private final String imagen;
    private final List<RespuestaDTO> respuestas;

    public PreguntaDTO(Long id, String pregunta, String imagen, List<RespuestaDTO> respuestas) {
        this.id = id;
        this.pregunta = pregunta;
        this.imagen = imagen;
        this.respuestas = respuestas;
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

    public static PreguntaDTO fromEntity(Pregunta pregunta) {
        List<RespuestaDTO> respuestasDTO = pregunta.getRespuestasMaestro().stream()
            .map(RespuestaDTO::fromEntity)
            .toList();

        return new PreguntaDTO(
            pregunta.getId(),
            pregunta.getPregunta(),
            pregunta.getImagen(),
            respuestasDTO
        );
    }
}
