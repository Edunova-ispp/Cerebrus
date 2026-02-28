package com.cerebrus.estadisticas;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.curso.Curso;

@Repository
public interface EstadisticasMaestroRepository {

    @Query("SELECT DISTINCT actAlumno FROM ActividadAlumno actAlumno JOIN FETCH actAlumno.alumno alumno " +
           "LEFT JOIN FETCH actAlumno.respuestasAlumno JOIN actAlumno.actividad act WHERE act.tema.curso = :curso")
    List<ActividadAlumno> findAllByCursoConRespuestas(@Param("curso") Curso curso);
    
}
