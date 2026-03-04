package com.cerebrus.usuario;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.organizacion.Organizacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "alumno")
public class Alumno extends Usuario {

    @Column(nullable = false)
    private Integer puntos;

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ActividadAlumno> actividadesAlumno = new ArrayList<>();

    // Constructores
    public Alumno() {
        super();
    }

    public Alumno(String nombre, String primerApellido, String segundoApellido,
                  String nombreUsuario, String correoElectronico, String contrasena, Integer puntos, Organizacion organizacion) {
        super(nombre, primerApellido, segundoApellido, nombreUsuario, correoElectronico, contrasena, organizacion);
        this.puntos = puntos;
    }

    // Getters y Setters
    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<ActividadAlumno> getActividadesAlumno() {
        return actividadesAlumno;
    }

    public void setActividadesAlumno(List<ActividadAlumno> actividadesAlumno) {
        this.actividadesAlumno = actividadesAlumno;
    }

    @Override
    public String toString() {
        return "Alumno{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", puntos=" + puntos +
                '}';
    }
}
