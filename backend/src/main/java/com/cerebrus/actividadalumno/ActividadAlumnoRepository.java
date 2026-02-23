package com.cerebrus.actividadalumno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadAlumnoRepository extends JpaRepository<ActividadAlumno, Long> {

}
