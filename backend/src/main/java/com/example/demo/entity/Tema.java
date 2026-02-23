package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tema")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    // Constructores
    public Tema() {
    }

    public Tema(String titulo, Curso curso) {
        this.titulo = titulo;
        this.curso = curso;
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

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    // Atributo derivado para obtener los tipos de actividades asociados al tema en orden
    public List<TipoAct> getActividades() {
        //TODO: Implementar lógica para obtener los tipos de actividades asociados al tema en orden
        List<TipoAct> actividades = new ArrayList<>();
        return actividades;
    }


    @Override
    public String toString() {
        return "Tema{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                '}';
    }
}
