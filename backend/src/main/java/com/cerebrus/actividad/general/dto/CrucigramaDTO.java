package com.cerebrus.actividad.general.dto;
import java.util.List;

import com.cerebrus.actividad.general.General;
import com.cerebrus.pregunta.dto.PreguntaDTO;

import lombok.Getter;

@Getter
public class CrucigramaDTO {
    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final Integer posicion;
    private final Long temaId;
    private final Boolean respVisible;
    private final List<PreguntaDTO> preguntas;
    public CrucigramaDTO(Long id, String titulo, String descripcion,  Integer posicion, Integer puntuacion, Boolean respVisible, Long temaId, List<PreguntaDTO> preguntas) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.posicion = posicion;
        this.temaId = temaId;
        this.respVisible = respVisible;
        this.preguntas = preguntas;
    }
    public static CrucigramaDTO fromEntity(General creada) {
        List<PreguntaDTO> preguntasDTO = creada.getPreguntas().stream()
            .map(PreguntaDTO::fromEntity)
            .toList();

        return new CrucigramaDTO(
            creada.getId(),
            creada.getTitulo(),
            creada.getDescripcion(),
            creada.getPosicion(),
            creada.getPuntuacion(),
            creada.getRespVisible(),
            creada.getTema().getId(),
            preguntasDTO
        );
    }




}
