package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.List;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
    ActividadAlumno corregirActividadAlumno(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds);
    void corregirNotaActividadAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota);
    void corregirRespuestasActividadAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds);
}
