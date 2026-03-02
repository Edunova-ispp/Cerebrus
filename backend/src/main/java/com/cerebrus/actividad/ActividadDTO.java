package com.cerebrus.actividad;

import lombok.Getter;

@Getter
public class ActividadDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final Integer posicion;

    public ActividadDTO(Actividad actividad) {
        this.id = actividad.getId();
        this.titulo = actividad.getTitulo();
        this.descripcion = actividad.getDescripcion();
        this.puntuacion = actividad.getPuntuacion();
        this.posicion = actividad.getPosicion();
    }
}
