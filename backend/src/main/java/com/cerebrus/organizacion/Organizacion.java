package com.cerebrus.organizacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.cerebrus.curso.Curso;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.usuario.Director;
import com.cerebrus.usuario.Maestro;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizacion")
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Maestro> maestros = new ArrayList<>();

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Curso> cursos = new ArrayList<>();

    @OneToOne(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Director director;

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Suscripcion> suscripciones = new ArrayList<>();

    // Constructores
    public Organizacion() {
    }

    public Organizacion(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Atributo derivado que se obtiene a partir de la fecha de fin de la última suscripción activa
    public Boolean getActivo() {
        if (suscripciones == null || suscripciones.isEmpty()) {
            return false;
        }
        Suscripcion ultimaSuscripcion = suscripciones.get(suscripciones.size() - 1);
        return ultimaSuscripcion.getFechaFin() == null || ultimaSuscripcion.getFechaFin().isAfter(LocalDate.now());
    }


    public List<Maestro> getMaestros() {
        return maestros;
    }

    public void setMaestros(List<Maestro> maestros) {
        this.maestros = maestros;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public Director getDirector() {
        return director;
    }

    public void setDirector(Director director) {
        this.director = director;
    }

    public List<Suscripcion> getSuscripciones() {
        return suscripciones;
    }

    public void setSuscripciones(List<Suscripcion> suscripciones) {
        this.suscripciones = suscripciones;
    }

    @Override
    public String toString() {
        return "Organizacion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", activo=" + getActivo() +
                '}';
    }
}
