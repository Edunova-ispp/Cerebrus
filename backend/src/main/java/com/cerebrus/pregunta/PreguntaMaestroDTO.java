package com.cerebrus.pregunta;

import java.util.List;

import com.cerebrus.respuestaMaestro.RespuestaMaestroDTO;

public class PreguntaMaestroDTO {

    private final Long id;
    private final String pregunta;
    private final String imagen;
    private final List<RespuestaMaestroDTO> respuestas;

    public PreguntaMaestroDTO(Long id, String pregunta, String imagen, List<RespuestaMaestroDTO> respuestas) {
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

    public List<RespuestaMaestroDTO> getRespuestas() {
        return respuestas;
    }
}
