package com.cerebrus.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuarioOrCorreoElectronico(String nombreUsuario, String correoElectronico);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
    Boolean existsByNombreUsuario(String nombreUsuario);
    Boolean existsByCorreoElectronico(String correoElectronico);
    
}
