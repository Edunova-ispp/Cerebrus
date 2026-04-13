package com.cerebrus.inscripcion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    
    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.id = :alumnoId AND i.curso.id = :cursoId")
    Inscripcion findByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.alumno WHERE i.curso.id = :cursoId")
    List<Inscripcion> findByCursoIdWithAlumno(Long cursoId);

}