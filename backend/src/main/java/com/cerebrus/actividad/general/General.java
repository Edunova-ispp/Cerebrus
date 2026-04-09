package com.cerebrus.actividad.general;

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
@Table(name = "general")
public class General extends Actividad {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoActGeneral tipo;

    //Relaciones
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();


    private Boolean mostrarPuntuacion = false;
    private Boolean permitirReintento = false;
    private Boolean encontrarRespuestaMaestro = false;
    private Boolean encontrarRespuestaAlumno = false;

    // Constructores
    public General() {
        super();
    }

    public General(String titulo, String descripcion, Integer puntuacion, String imagen,
                   Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Tema tema, TipoActGeneral tipo,
                    Boolean mostrarPuntuacion, Boolean permitirReintento, Boolean encontrarRespuestaMaestro, Boolean encontrarRespuestaAlumno) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.tipo = tipo;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
    }

    public General(String titulo, String descripcion, Integer puntuacion, String imagen,
                   Boolean respVisible, Integer posicion, Integer version, Tema tema, TipoActGeneral tipo,
                    Boolean mostrarPuntuacion, Boolean permitirReintento, Boolean encontrarRespuestaMaestro, Boolean encontrarRespuestaAlumno) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema);
        this.tipo = tipo;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
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

    public Boolean getMostrarPuntuacion() {
        return mostrarPuntuacion;
    }

    public void setMostrarPuntuacion(Boolean mostrarPuntuacion) {
        this.mostrarPuntuacion = mostrarPuntuacion;
    }

    public Boolean getPermitirReintento() {
        return permitirReintento;
    }

    public void setPermitirReintento(Boolean permitirReintento) {
        this.permitirReintento = permitirReintento;
    }

    public Boolean getEncontrarRespuestaMaestro() {
        return encontrarRespuestaMaestro;
    }

    public void setEncontrarRespuestaMaestro(Boolean encontrarRespuestaMaestro) {
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
    }

    public Boolean getEncontrarRespuestaAlumno() {
        return encontrarRespuestaAlumno;
    }

    public void setEncontrarRespuestaAlumno(Boolean encontrarRespuestaAlumno) {
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
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
