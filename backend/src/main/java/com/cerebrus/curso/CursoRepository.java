package com.cerebrus.curso;

import java.util.List;

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
    @Query("SELECT c FROM Curso c WHERE c.id = :id")
	Curso findByID(@Param("id") Long id);

	boolean existsByCodigo(String codigo);
    @Query("SELECT c FROM Curso c WHERE c.codigo = :codigo")
	Curso findByCodigo(@Param("codigo") String codigo);
}
