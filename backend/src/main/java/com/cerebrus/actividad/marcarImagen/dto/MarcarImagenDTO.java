package com.cerebrus.actividad.marcarImagen.dto;

import java.util.List;

import com.cerebrus.puntoImage.dto.PuntoImagenDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarcarImagenDTO {
    
    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final String imagenActividad;
    private final Boolean respVisible;
    private final String comentariosRespVisible;
    private final Long temaId;
    private final String imagenAMarcar;
    private final List<PuntoImagenDTO> puntosImagen;

    public MarcarImagenDTO(
        Long id,
        String titulo,
        String descripcion,
        Integer puntuacion,
        String imagenActividad,
        Boolean respVisible,
        String comentariosRespVisible,
        Long temaId,
        String imagenAMarcar,
        List<PuntoImagenDTO> puntosImagen
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagenActividad = imagenActividad;
        this.respVisible = respVisible;
        this.comentariosRespVisible = comentariosRespVisible;
        this.temaId = temaId;
        this.imagenAMarcar = imagenAMarcar;
        this.puntosImagen = puntosImagen;
    }
}
