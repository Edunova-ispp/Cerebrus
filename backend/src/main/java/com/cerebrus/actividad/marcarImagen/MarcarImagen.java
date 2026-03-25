package com.cerebrus.actividad.marcarImagen;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.puntoImage.PuntoImagen;
import com.cerebrus.tema.Tema;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcar_imagen")
public class MarcarImagen extends Actividad {

    @Column(nullable = false, name = "imagen_a_marcar")
    private String imagenAMarcar;

    //Relaciones
    @OneToMany(mappedBy = "marcarImagen", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PuntoImagen> puntosImagen = new ArrayList<>();

    // Constructores
    public MarcarImagen() {
        super();
    }

    public MarcarImagen(String titulo, String descripcion, Integer puntuacion, String imagen,
                        Boolean respVisible, Integer posicion, Integer version, Tema tema, String imagenAMarcar) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.imagenAMarcar = imagenAMarcar;
    }

    // Getters y Setters
    public String getImagenAMarcar() {
        return imagenAMarcar;
    }

    public void setImagenAMarcar(String imagenAMarcar) {
        this.imagenAMarcar = imagenAMarcar;
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
                ", imagenAMarcar='" + getImagenAMarcar() + '\'' +
                '}';
    }
}
