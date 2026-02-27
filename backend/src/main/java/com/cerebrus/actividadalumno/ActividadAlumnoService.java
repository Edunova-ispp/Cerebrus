package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
    ActividadAlumno corregirPuntuacionActividadAlumno(Long id, Integer nuevaPuntuacion);
}
