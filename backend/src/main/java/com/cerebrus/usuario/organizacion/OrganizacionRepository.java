package com.cerebrus.usuario.organizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    boolean existsByNombreCentro(String nombreCentro);

    boolean existsByCodigoVerificacion(Integer codigoVerificacion);

    java.util.Optional<Organizacion> findByCodigoVerificacion(Integer codigoVerificacion);

}
