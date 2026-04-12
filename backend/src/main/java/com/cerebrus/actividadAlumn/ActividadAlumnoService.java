package com.cerebrus.actividadAlumn;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ActividadAlumnoService {
    ActividadAlumno crearActAlumno(Integer puntuacion, LocalDateTime fechaInicio,
        LocalDateTime fechaFin,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno encontrarActAlumnoPorId(Long id);
    Optional<ActividadAlumno> encontrarActAlumnoPorAlumnoIdYActId(Long alumnoId, Long actividadId);
    ActividadAlumno actualizarActAlumno(Long id, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void eliminarActAlumnoPorId(Long id);
    Integer existeActAlumnoPorActIdYCurrentUserId(Long actividadId);
    ActividadAlumno abandonarActAlumnoPorId(Long actividadAlumnoId);
    ActividadAlumno corregirActAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds);
    void corregirNotaActAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota);
    void corregirRespuestasActAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds);
    ActividadAlumno corregirActAlumnoAutomaticamente(Long id, List<Long> respuestasIds);
    ActividadAlumno corregirActAlumnoAutomaticamenteClasificacion(Long actividadAlumnoId, List<Long> respuestasIds);
}
