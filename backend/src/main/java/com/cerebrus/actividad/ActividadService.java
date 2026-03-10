package com.cerebrus.actividad;

import java.util.List;

public interface ActividadService {

    Actividad crearActividadTeoria(String titulo, String descripcion, Integer puntuacion, String imagen, Long temaId);
    List<Actividad> ObtenerActividadesPorTema(Long temaId);

    void deleteActividad(Long id);
    Actividad updateActividadTeoria(Long id, String titulo, String descripcion);
    Actividad encontrarActividadPorIdMaestro(Long id);
    Actividad encontrarActividadPorIdAlumno(Long id);


}
