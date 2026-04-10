package com.cerebrus.suscripcion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    Optional<Suscripcion> findByIdAndOrganizacionId(Long id, Long organizacionId);
    
    List<Suscripcion> findByOrganizacionId(Long organizacionId);

    @Query("SELECT s FROM Suscripcion s WHERE s.organizacion.id = :organizacionId " +
       "AND s.estadoPago = com.cerebrus.comun.enumerados.EstadoPagoSuscripcion.PAGADA " +
       "AND CURRENT_DATE BETWEEN s.fechaInicio AND s.fechaFin")
    Optional<Suscripcion> findByOrganizacionIdSuscripcionActiva(@Param("organizacionId") Long organizacionId);

    Optional<Suscripcion> findByTransaccionId(String transaccionId);
}
