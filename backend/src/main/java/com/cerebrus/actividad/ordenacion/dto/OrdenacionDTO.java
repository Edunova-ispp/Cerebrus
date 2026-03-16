package com.cerebrus.actividad.ordenacion.dto;

import java.util.List;

public class OrdenacionDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final String imagen;
    private final Boolean respVisible;
    private final String comentariosRespVisible;
    private final Integer posicion;
    private final Long temaId;
    private final List<String> valores;

    public OrdenacionDTO(
        Long id,
        String titulo,
        String descripcion,
        Integer puntuacion,
        String imagen,
        Boolean respVisible,
        String comentariosRespVisible,
        Integer posicion,
        Long temaId,
        List<String> valores
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagen = imagen;
        this.respVisible = respVisible;
        this.comentariosRespVisible = comentariosRespVisible;
        this.posicion = posicion;
        this.temaId = temaId;
        this.valores = valores;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public String getImagen() {
        return imagen;
    }

    public Boolean getRespVisible() {
        return respVisible;
    }

    public String getComentariosRespVisible() {
        return comentariosRespVisible;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public Long getTemaId() {
        return temaId;
    }

    public List<String> getValores() {
        return valores;
    }
}
