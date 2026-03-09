package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import com.cerebrus.actividad.Actividad;

public interface ActividadAlumnoService {
    ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada,Integer nota, Integer numAbandonos, Long alumnoId, Long actId);
    ActividadAlumno readActividadAlumno(Long id);
    Optional<ActividadAlumno> readActividadAlumnoByAlumnoIdAndActividadId(Long alumnoId, Long actividadId);
    Integer ensureActividadAlumno(Long actividadId);
        ActividadAlumno abandonarActividadAlumno(Long actividadAlumnoId);
    ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos);
    void deleteActividadAlumno(Long id);
    ActividadAlumno corregirActividadAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds);
    void corregirNotaActividadAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota);
    void corregirRespuestasActividadAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds);
    ActividadAlumno corregirActividadAlumnoAutomaticamente(Long id, List<Long> respuestasIds);
    void corregirActividadAlumnoAutomaticamenteGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
    void corregirActividadAlumnoAutomaticamenteCartaGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
    void corregirActividadAlumnoAutomaticamenteOrdenacion(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
    ActividadAlumno corregirActividadAlumnoAutomaticamenteGeneralClasificacion(Long actividadAlumnoId, List<Long> respuestasIds);
    void corregirActividadAlumnoAutomaticamenteMarcarImagen(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad);
}
