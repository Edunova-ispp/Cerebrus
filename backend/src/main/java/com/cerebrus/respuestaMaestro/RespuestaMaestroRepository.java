package com.cerebrus.respuestaMaestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaMaestroRepository extends JpaRepository<RespuestaMaestro, Long> {

    @Query("select r from RespuestaMaestro r where r.respuesta = :respuesta")
    Optional<RespuestaMaestro> findByRespuesta(@Param("respuesta") String respuesta);

    @Query("select r from RespuestaMaestro r where r.respuesta = :respuesta and r.pregunta.id = :preguntaId")
    Optional<RespuestaMaestro> findByRespuestaAndPreguntaId(@Param("respuesta") String respuesta, @Param("preguntaId") Long preguntaId);

    @Query("select r from RespuestaMaestro r where r.pregunta.id = :preguntaId")
    List<RespuestaMaestro> findRespuestaByPreguntaId(@Param("preguntaId") Long preguntaId);

}
