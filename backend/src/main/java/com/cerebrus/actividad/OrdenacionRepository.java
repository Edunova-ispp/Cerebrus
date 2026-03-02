package com.cerebrus.actividad;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenacionRepository extends JpaRepository<Ordenacion, Long> {

	@EntityGraph(attributePaths = {"valores", "tema"})
	Optional<Ordenacion> findWithValoresById(Long id);

}
