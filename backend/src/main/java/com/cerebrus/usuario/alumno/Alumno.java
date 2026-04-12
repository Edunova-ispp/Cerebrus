package com.cerebrus.usuario.alumno;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.organizacion.Organizacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "alumno")
public class Alumno extends Usuario {

    @Column(nullable = false)
    private Integer puntos;

    //Relaciones
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ActividadAlumno> actividadesAlumno = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id")
    private Organizacion organizacion;

    // Constructores
    public Alumno() {
        super();
    }

    public Alumno(String nombre, String primerApellido, String segundoApellido,
                  String nombreUsuario, String correoElectronico, String contrasena, Integer puntos, Organizacion organizacion) {
        super(nombre, primerApellido, segundoApellido, nombreUsuario, correoElectronico, contrasena, organizacion);
        this.puntos = puntos;
        this.organizacion = organizacion;
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

    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
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
