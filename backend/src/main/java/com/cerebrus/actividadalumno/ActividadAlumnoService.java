package com.cerebrus.actividadalumno;


public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, java.time.LocalDate fecha, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion, java.time.LocalDate fecha);
    void deleteActividadAlumno(Long id);
    ActividadAlumno corregirPuntuacionActividadAlumno(Long id, Integer nuevaPuntuacion);
}
