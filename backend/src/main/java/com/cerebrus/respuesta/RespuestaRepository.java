package com.cerebrus.respuesta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    @Query("select r from Respuesta r where r.respuesta = :respuesta")
    Optional<Respuesta> findByRespuesta(@Param("respuesta") String respuesta);

}
