package com.cerebrus.puntoImagen;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoImagenRepository extends JpaRepository<PuntoImagen, Long> {

    @Query("SELECT p FROM PuntoImagen p WHERE p.marcarImagen.id = :marcarImagenId AND p.pixelX = :pixelX AND p.pixelY = :pixelY")
    Optional<PuntoImagen> findByMarcarImagenIdAndPixelXAndPixelY(@Param("marcarImagenId") Long marcarImagenId, @Param("pixelX") Integer pixelX, @Param("pixelY") Integer pixelY);

}
