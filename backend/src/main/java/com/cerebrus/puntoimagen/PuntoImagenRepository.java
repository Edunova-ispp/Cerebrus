package com.cerebrus.puntoimagen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoImagenRepository extends JpaRepository<PuntoImagen, Long> {

}
