package com.cerebrus.actividad;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.tema.Tema;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordenacion")
public class Ordenacion extends Actividad {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ordenacion_valores", joinColumns = @JoinColumn(name = "ordenacion_id"))
    @Column(name = "valor")
    @OrderColumn(name = "orden")
    private List<String> valores = new ArrayList<>();

    // Constructores
    public Ordenacion() {
        super();
    }

    public Ordenacion(String titulo, String descripcion, Integer puntuacion, String imagen,
                      Boolean respVisible, Integer posicion, Integer version, Tema tema, List<String> valores) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.valores = valores;
    }

    // Getters y Setters
    public List<String> getValores() {
        return valores;
    }

    public void setValores(List<String> valores) {
        this.valores = valores;
    }

    @Override
    public String toString() {
        return "Ordenacion{" +
                "id=" + getId() +
                ", titulo='" + getTitulo() + '\'' +
                ", valores=" + valores +
                '}';
    }
}
