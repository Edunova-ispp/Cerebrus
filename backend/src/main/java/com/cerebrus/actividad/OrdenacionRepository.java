package com.cerebrus.actividad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenacionRepository extends JpaRepository<Ordenacion, Long> {

}
