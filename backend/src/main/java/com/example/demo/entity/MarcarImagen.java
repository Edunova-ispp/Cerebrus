package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcar_imagen")
public class MarcarImagen extends Actividad {

    @Column(nullable = false)
    private String imagen;

    @OneToMany(mappedBy = "marcarImagen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PuntoImagen> puntosImagen = new ArrayList<>();

    // Constructores
    public MarcarImagen() {
        super();
    }

    public MarcarImagen(String titulo, String descripcion, Integer puntuacion, String imagenActividad,
                        Boolean respVisible, Integer posicion, Integer version, Tema tema, String imagen) {
        super(titulo, descripcion, puntuacion, imagenActividad, respVisible, posicion, version, tema);
        this.imagen = imagen;
    }

    // Getters y Setters
    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<PuntoImagen> getPuntosImagen() {
        return puntosImagen;
    }

    public void setPuntosImagen(List<PuntoImagen> puntosImagen) {
        this.puntosImagen = puntosImagen;
    }

    @Override
    public String toString() {
        return "MarcarImagen{" +
                "id=" + getId() +
                ", titulo='" + getTitulo() + '\'' +
                ", imagen='" + imagen + '\'' +
                '}';
    }
}
