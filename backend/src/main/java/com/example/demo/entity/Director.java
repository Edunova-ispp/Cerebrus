package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "director")
public class Director extends Usuario {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false, unique = true)
    private Organizacion organizacion;

    // Constructores
    public Director() {
        super();
    }

    public Director(String nombre, String primerApellido, String segundoApellido,
                    String nombreUsuario, String correoElectronico, String contrasena,
                    Organizacion organizacion) {
        super(nombre, primerApellido, segundoApellido, nombreUsuario, correoElectronico, contrasena);
        this.organizacion = organizacion;
    }

    // Getters y Setters
    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    @Override
    public String toString() {
        return "Director{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", organizacion=" + (organizacion != null ? organizacion.getId() : null) +
                '}';
    }
}
