package com.cerebrus.actividadalumno;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cerebrus.usuario.alumno.Alumno;

@Repository
public interface ActividadAlumnoRepository extends JpaRepository<ActividadAlumno, Long> {

    @Query("SELECT aa FROM ActividadAlumno aa WHERE aa.alumno.id = :alumnoId AND aa.actividad.id = :actividadId")
    Optional<ActividadAlumno> findByAlumnoIdAndActividadId(Long alumnoId, Long actividadId);

    @Query("SELECT aa.inicio AS inicio, aa.acabada AS acabada FROM ActividadAlumno aa WHERE aa.alumno = :alumno AND aa.actividad.tema.curso.id = :cursoId")
    List<ActividadAlumnoProgreso> findProgresoByAlumnoAndCursoId(@Param("alumno") Alumno alumno, @Param("cursoId") Long cursoId);

    @Query("SELECT aa.actividad FROM ActividadAlumno aa WHERE aa.id = :actividadId")
    List<ActividadAlumno> findByActividadId(@Param("actividadId") Long actividadId);

    @Query("SELECT aa FROM ActividadAlumno aa WHERE aa.actividad.tema.curso.id = :cursoId")
    List<ActividadAlumno> findByCursoID(Long cursoId);



}
