package com.cerebrus.respuestaAlumno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaAlumnoRepository extends JpaRepository<RespuestaAlumno, Long> {

}
