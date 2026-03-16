package com.cerebrus.respuestaAlumno.respAlumGeneral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespAlumnoGeneralRepository extends JpaRepository<RespAlumnoGeneral, Long> {

}
