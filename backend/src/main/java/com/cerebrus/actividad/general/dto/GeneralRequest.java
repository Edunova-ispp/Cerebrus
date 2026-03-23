package com.cerebrus.actividad.general.dto;

import java.util.List;

import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralRequest {

    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final String imagen;
    private final Boolean respVisible;
    private final String comentariosRespVisible;
    private final Tema tema;
    private final List<Pregunta> preguntas;
    

    public GeneralRequest(
            String titulo,
            String descripcion,
            Integer puntuacion,
            String imagen,
            Boolean respVisible,
            String comentariosRespVisible,
            Tema tema,
            List<Pregunta> preguntas
            ) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagen = imagen;
        this.respVisible = respVisible;
        this.comentariosRespVisible = comentariosRespVisible;
        this.preguntas = preguntas;
        this.tema = tema;
        
    }
}
