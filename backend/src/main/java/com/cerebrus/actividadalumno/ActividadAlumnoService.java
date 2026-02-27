package com.cerebrus.actividadalumno;

import java.time.LocalDate;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDate fecha, LocalDate inicio,
        LocalDate acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion, LocalDate fecha,
         LocalDate inicio, LocalDate acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
}
