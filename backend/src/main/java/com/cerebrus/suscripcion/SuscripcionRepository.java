package com.cerebrus.suscripcion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

}
