package com.cerebrus.pregunta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    @Query("SELECT p FROM Pregunta p  WHERE p.actividad.id = :actividadId")
    List<Pregunta> findByActividadId(Long actividadId);

}
