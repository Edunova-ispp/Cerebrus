package com.cerebrus.actividad.tablero.dto;

import java.util.List;

import com.cerebrus.comun.enumerados.TamanoTablero;
import com.cerebrus.actividad.tablero.Tablero;
import com.cerebrus.pregunta.PreguntaDTO;

import lombok.Getter;

@Getter
public class TableroDTO {
    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final Boolean tamano;
    private final Integer posicion;
    private final Long temaId;
    private final Boolean respVisible;
    private final List<PreguntaDTO> preguntas;
    public TableroDTO(Long id, String titulo, String descripcion, Boolean tamano, Integer posicion, Integer puntuacion, Boolean respVisible, Long temaId, List<PreguntaDTO> preguntas) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.tamano = tamano; // Verdadero para 3x3, falso para 4x4
        this.posicion = posicion;
        this.temaId = temaId;
        this.respVisible = respVisible;
        this.preguntas = preguntas;
    }
    public static TableroDTO fromEntity(Tablero creada) {
        List<PreguntaDTO> preguntasDTO = creada.getPreguntas().stream()
            .map(PreguntaDTO::fromEntity)
            .toList();

        return new TableroDTO(
            creada.getId(),
            creada.getTitulo(),
            creada.getDescripcion(),
            creada.getTamano() == TamanoTablero.TRES_X_TRES,
            creada.getPosicion(),
            creada.getPuntuacion(),
            creada.getRespVisible(),
            creada.getTema().getId(),
            preguntasDTO
        );
    }




}
