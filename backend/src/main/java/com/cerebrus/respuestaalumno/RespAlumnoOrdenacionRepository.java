package com.cerebrus.respuestaalumno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespAlumnoOrdenacionRepository extends JpaRepository<RespAlumnoOrdenacion, Long> {

}
