package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.List;

import com.cerebrus.actividad.Actividad;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
    ActividadAlumno corregirActividadAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds);
    void corregirNotaActividadAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota);
    void corregirRespuestasActividadAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds);
    ActividadAlumno corregirActividadAlumnoAutomaticamente(Long id, List<Long> respuestasIds);
    void corregirActividadAlumnoAutomaticamenteGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
    void corregirActividadAlumnoAutomaticamenteOrdenacion(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
}
