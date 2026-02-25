package com.cerebrus.actividad;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @Query("SELECT MAX(a.posicion) FROM Actividad a WHERE a.tema.id = :temaId")
    Integer findMaxPosicionByTemaId(@Param("temaId") Long temaId);

    @Query("SELECT a FROM Actividad a WHERE a.tema.id = :temaId")
    List<Actividad> findByTemaId(@Param("temaId") Long temaId);
}
