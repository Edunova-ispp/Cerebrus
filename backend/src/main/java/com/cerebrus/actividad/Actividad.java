package com.cerebrus.actividad;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.tema.Tema;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "actividad")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Integer puntuacion;

    private String imagen;

    @Column(nullable = false)
    private Boolean respVisible;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ActividadAlumno> actividadesAlumno = new ArrayList<>();

    // Constructores
    public Actividad() {
    }

    public Actividad(String titulo, String descripcion, Integer puntuacion, String imagen,
                     Boolean respVisible, Integer posicion, Integer version, Tema tema) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagen = imagen;
        this.respVisible = respVisible;
        this.posicion = posicion;
        this.version = version;
        this.tema = tema;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getRespVisible() {
        return respVisible;
    }

    public void setRespVisible(Boolean respVisible) {
        this.respVisible = respVisible;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public List<ActividadAlumno> getActividadesAlumno() {
        return actividadesAlumno;
    }

    public void setActividadesAlumno(List<ActividadAlumno> actividadesAlumno) {
        this.actividadesAlumno = actividadesAlumno;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", puntuacion=" + puntuacion +
                ", posicion=" + posicion +
                ", version=" + version +
                '}';
    }
}
