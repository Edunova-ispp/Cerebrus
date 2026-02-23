package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "general")
public class General extends Actividad {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoActGeneral tipo;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    // Constructores
    public General() {
        super();
    }

    public General(String titulo, String descripcion, Integer puntuacion, String imagen,
                   Boolean respVisible, Integer posicion, Integer version, Tema tema, TipoActGeneral tipo) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.tipo = tipo;
    }

    // Getters y Setters
    public TipoActGeneral getTipo() {
        return tipo;
    }

    public void setTipo(TipoActGeneral tipo) {
        this.tipo = tipo;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    @Override
    public String toString() {
        return "General{" +
                "id=" + getId() +
                ", titulo='" + getTitulo() + '\'' +
                ", tipo=" + tipo +
                '}';
    }
}
