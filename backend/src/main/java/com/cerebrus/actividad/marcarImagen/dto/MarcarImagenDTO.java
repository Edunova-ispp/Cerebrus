package com.cerebrus.actividad.marcarImagen.dto;

import java.util.List;

import com.cerebrus.puntoImage.dto.PuntoImagenDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarcarImagenDTO {
    
    private final Long id;
    @NotBlank
    private final String titulo;
    private final String descripcion;
    @NotNull
    @Min(1)
    private final Integer puntuacion;
    private final String imagenActividad;
    @NotNull
    private final Boolean respVisible;
    private final String comentariosRespVisible;
    @NotNull
    private final Long temaId;
    @NotBlank
    private final String imagenAMarcar;
    @NotNull
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
