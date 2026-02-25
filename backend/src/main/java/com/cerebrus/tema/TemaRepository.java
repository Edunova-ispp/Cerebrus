package com.cerebrus.tema;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    @Query("SELECT t FROM Tema t WHERE t.curso.id = :cursoId")
    List<Tema> findByCursoId(@Param("cursoId") Integer cursoId);

}
