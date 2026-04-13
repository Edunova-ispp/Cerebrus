package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespAlumnoOrdenacionRepository extends JpaRepository<RespAlumnoOrdenacion, Long> {

	Optional<RespAlumnoOrdenacion> findTopByActividadAlumnoIdOrderByIdDesc(Long actividadAlumnoId);

}
