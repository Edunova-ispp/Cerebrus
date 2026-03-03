package com.cerebrus.actividad;

import lombok.Getter;

@Getter
public class ActividadDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final Integer posicion;
    private final String tipo;

    public ActividadDTO(Actividad actividad) {
        this.id = actividad.getId();
        this.titulo = actividad.getTitulo();
        this.descripcion = actividad.getDescripcion();
        this.puntuacion = actividad.getPuntuacion();
        this.posicion = actividad.getPosicion();
        this.tipo = actividad instanceof General ? "general"
                  : actividad instanceof Ordenacion ? "ordenacion"
                  : actividad instanceof MarcarImagen ? "marcarImagen"
                  : "otro";
    }
}
