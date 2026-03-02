package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    Optional<ActividadAlumno> readActividadAlumnoByAlumnoIdAndActividadId(Long alumnoId, Long actividadId);
    Integer ensureActividadAlumno(Long actividadId);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
}
