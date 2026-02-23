package com.cerebrus.pregunta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

}
