package com.cerebrus.actividad.DTO;

import com.cerebrus.comun.enumerados.*;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.tablero.Tablero;

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
        this.posicion = actividad.getPosicion();
        if (actividad instanceof General) {
            this.tipo = ((General) actividad).getTipo().toString().toLowerCase();
        } else if (actividad instanceof Ordenacion) {
            this.tipo = "ordenacion";
        } else if (actividad instanceof Tablero) {
            this.tipo = "tablero";
        } else if (actividad instanceof MarcarImagen) {
            this.tipo = "marcarImagen";
        } else {
            this.tipo = "otro";
        }
        if (tipo != TipoActGeneral.TEORIA.toString().toLowerCase()) {  
            this.puntuacion = actividad.getPuntuacion();
        } else {
            this.puntuacion = 0;
        }
    }
}
