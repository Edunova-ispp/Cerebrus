package com.cerebrus.actividad.tablero;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.comun.enumerados.*;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;

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

    private Boolean mostrarPuntuacion = false;
    private Boolean permitirReintento = false;
    private Boolean encontrarRespuestaMaestro = false;
    private Boolean encontrarRespuestaAlumno = false;

    //Relaciones
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    // Constructores
    public Tablero() {
        super();
    }

    public Tablero(String titulo, String descripcion, Integer puntuacion, String imagen,
                   Boolean respVisible, Integer posicion, Integer version, Tema tema, TamanoTablero tamano,
                   Boolean mostrarPuntuacion, Boolean permitirReintento, Boolean encontrarRespuestaMaestro, Boolean encontrarRespuestaAlumno) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.tamano = tamano;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
    }

    // Getters y Setters
    public TamanoTablero getTamano() {
        return tamano;
    }

    public void setTamano(TamanoTablero tamano) {
        this.tamano = tamano;
    }

    public Boolean getMostrarPuntuacion() {
        return mostrarPuntuacion;
    }

    public void setMostrarPuntuacion(Boolean mostrarPuntuacion) {
        this.mostrarPuntuacion = mostrarPuntuacion != null ? mostrarPuntuacion : false;
    }

    public Boolean getPermitirReintento() {
        return permitirReintento;
    }

    public void setPermitirReintento(Boolean permitirReintento) {
        this.permitirReintento = permitirReintento != null ? permitirReintento : false;
    }

    public Boolean getEncontrarRespuestaMaestro() {
        return encontrarRespuestaMaestro;
    }

    public void setEncontrarRespuestaMaestro(Boolean encontrarRespuestaMaestro) {
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro != null ? encontrarRespuestaMaestro : false;
    }

    public Boolean getEncontrarRespuestaAlumno() {
        return encontrarRespuestaAlumno;
    }

    public void setEncontrarRespuestaAlumno(Boolean encontrarRespuestaAlumno) {
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno != null ? encontrarRespuestaAlumno : false;
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
