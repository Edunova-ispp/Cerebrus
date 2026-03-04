package com.cerebrus.actividad;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralRepository extends JpaRepository<General, Long> {

    @Query("""
        select g from General g
        left join fetch g.preguntas
        where g.id = :id
    """)
    Optional<General> findByIdWithPreguntas(@Param("id") Long id);

}
