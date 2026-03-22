package com.cerebrus.suscripcion;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

	Optional<Suscripcion> findTopByOrganizacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
			Long organizacionId,
			LocalDate fechaInicio,
			LocalDate fechaFin);

}
