package com.cerebrus.usuario;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.curso.Curso;
import com.cerebrus.organizacion.Organizacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "maestro")
public class Maestro extends Usuario {

    @OneToMany(mappedBy = "maestro", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Curso> cursos = new ArrayList<>();

    // Constructores
    public Maestro() {
        super();
    }

    public Maestro(String nombre, String primerApellido, String segundoApellido,
                   String nombreUsuario, String correoElectronico, String contrasena,
                   Organizacion organizacion) {
        super(nombre, primerApellido, segundoApellido, nombreUsuario, correoElectronico, contrasena, organizacion);
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
                ", organizacion=" + (getOrganizacion() != null ? getOrganizacion().getId() : null) +
                '}';
    }
}
