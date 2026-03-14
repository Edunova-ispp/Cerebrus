package com.cerebrus.respuestaAlumno.respAlumPuntoImagen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cerebrus.actividadalumno.ActividadAlumno;

@Repository
public interface RespAlumnoPuntoImagenRepository extends JpaRepository<RespAlumnoPuntoImagen, Long> {

    @Query("SELECT actAlum FROM ActividadAlumno actAlum WHERE actAlum.id = :actividadAlumnoId")
    ActividadAlumno encontrarActividadAlumnoPorId(@Param("actividadAlumnoId") Long actividadAlumnoId);

}
