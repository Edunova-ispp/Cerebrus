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
@Table(name = "tablero")
public class Tablero extends Actividad {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TamanoTablero tamano;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    // Constructores
    public Tablero() {
        super();
    }

    public Tablero(String titulo, String descripcion, Integer puntuacion, String imagen,
                   Boolean respVisible, Integer posicion, Integer version, Tema tema, TamanoTablero tamano) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.tamano = tamano;
    }

    // Getters y Setters
    public TamanoTablero getTamano() {
        return tamano;
    }

    public void setTamano(TamanoTablero tamano) {
        this.tamano = tamano;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public boolean validarNumeroPreguntas() {
        if(tamano==TamanoTablero.TRES_X_TRES) {
            return preguntas.size() == 8;
        } else if(tamano==TamanoTablero.CUATRO_X_CUATRO) {
            return preguntas.size() == 15;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Tablero{" +
                "id=" + getId() +
                ", titulo='" + getTitulo() + '\'' +
                ", tamano=" + tamano +
                '}';
    }
}
