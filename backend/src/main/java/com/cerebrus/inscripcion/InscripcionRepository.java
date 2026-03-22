package com.cerebrus.inscripcion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    
    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.id = :alumnoId AND i.curso.id = :cursoId")
    Inscripcion findByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoId(Long alumnoId);

    @Query("SELECT COUNT(DISTINCT i.alumno.id) FROM Inscripcion i WHERE i.alumno.organizacion.id = :organizacionId")
    long countDistinctAlumnosInscritosByOrganizacionId(Long organizacionId);

}