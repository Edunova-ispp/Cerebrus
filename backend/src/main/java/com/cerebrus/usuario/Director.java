package com.cerebrus.usuario;

import com.cerebrus.organizacion.Organizacion;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "director")
public class Director extends Usuario {

    // Constructores
    public Director() {
        super();
    }

    public Director(String nombre, String primerApellido, String segundoApellido,
                    String nombreUsuario, String correoElectronico, String contrasena,
                    Organizacion organizacion) {
        super(nombre, primerApellido, segundoApellido, nombreUsuario, correoElectronico, contrasena, organizacion);
    }

    @Override
    public String toString() {
        return "Director{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", organizacion=" + (getOrganizacion() != null ? getOrganizacion().getId() : null) +
                '}';
    }
}
