package com.cerebrus.actividad.marcarImagen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarcarImagenRepository extends JpaRepository<MarcarImagen, Long> {

}
