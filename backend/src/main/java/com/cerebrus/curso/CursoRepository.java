package com.cerebrus.curso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

	
    @Query("SELECT c FROM Curso c WHERE c.maestro.id = :maestroId")
	List<Curso> findByMaestroId(@Param("maestroId") Long maestroId);

	@Query("SELECT curso FROM Inscripcion i WHERE i.alumno.id = :alumnoId AND i.curso.visibilidad = true")
	List<Curso> findByAlumnoId(@Param("alumnoId") Long alumnoId);
}
