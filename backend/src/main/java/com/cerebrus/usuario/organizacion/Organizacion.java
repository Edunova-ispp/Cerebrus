package com.cerebrus.usuario.organizacion;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizacion")
public class Organizacion extends Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCentro;

    @Column(nullable = false)
    private Boolean emailConfirmado = false;

    @Column(nullable = false)
    private Integer codigoVerificacion;

    // Relaciones

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Maestro> maestros = new ArrayList<>();

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Alumno> alumnos = new ArrayList<>();

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Suscripcion> suscripciones = new ArrayList<>();

    // Constructores
    public Organizacion() {
    }

    public Organizacion(String nombre) {
        this.nombreCentro = nombre;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCentro() {
        return nombreCentro;
    }

    public void setNombreCentro(String nombre) {
        this.nombreCentro = nombre;
    }

    public List<Maestro> getMaestros() {
        return maestros;
    }

    public void setMaestros(List<Maestro> maestros) {
        this.maestros = maestros;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    public List<Suscripcion> getSuscripciones() {
        return suscripciones;
    }

    public void setSuscripciones(List<Suscripcion> suscripciones) {
        this.suscripciones = suscripciones;
    }

    public Boolean getEmailConfirmado() {
        return emailConfirmado;
    }

    public Integer getCodigoVerificacion() {
        return codigoVerificacion;
    }
    public void setEmailConfirmado(Boolean emailConfirmado) {
        this.emailConfirmado = emailConfirmado;
    }
    public void setCodigoVerificacion(Integer codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    // Atributo derivado. Indica si la organización tiene una suscripción activa.
    public Boolean getActivo() {
        if (suscripciones == null || suscripciones.isEmpty()) {
            return false;
        }
        Suscripcion ultimaSuscripcion = suscripciones.get(suscripciones.size() - 1);
        return ultimaSuscripcion.isActiva();
    }

    @Override
    public String toString() {
        return "Organizacion{" +
                "id=" + id +
                ", nombre='" + nombreCentro + '\'' +
                ", activo=" + getActivo() +
                '}';
    }
}
