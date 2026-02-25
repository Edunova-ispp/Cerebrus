package com.cerebrus.actividad;

import java.util.List;

public interface ActividadService {

    Actividad crearActividadTeoria(String titulo, String descripcion, Integer puntuacion, String imagen, Long temaId, Long maestroId);

    List<Actividad> ObtenerActividadesPorTema(Long temaId);
}
