package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "maestro")
public class Maestro extends Usuario {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @OneToMany(mappedBy = "maestro", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Curso> cursos = new ArrayList<>();

    // Constructores
    public Maestro() {
        super();
    }

    public Maestro(String nombre, String primerApellido, String segundoApellido,
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

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    @Override
    public String toString() {
        return "Maestro{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", organizacion=" + (organizacion != null ? organizacion.getId() : null) +
                '}';
    }
}
