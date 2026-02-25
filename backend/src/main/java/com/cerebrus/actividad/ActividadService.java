package com.cerebrus.actividad;

public interface ActividadService {

    Actividad crearActividadTeoria(String titulo, String descripcion, Integer puntuacion, String imagen, Long temaId, Long maestroId);
}
