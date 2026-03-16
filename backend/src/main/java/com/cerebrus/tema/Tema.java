package com.cerebrus.tema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cerebrus.comun.enumerados.*;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.curso.Curso;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tema")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    //Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Actividad> actividades = new ArrayList<>();

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

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades){
        this.actividades = actividades;
    }

    // Atributo derivado para obtener los tipos de actividades asociados al tema en orden. No modificable
    public List<TipoAct> getTipoActividades() {
        List<TipoAct> actividades = new ArrayList<>(Arrays.asList(TipoAct.values()));
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
