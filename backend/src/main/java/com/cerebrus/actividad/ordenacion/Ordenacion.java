package com.cerebrus.actividad.ordenacion;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacion;
import com.cerebrus.tema.Tema;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordenacion")
public class Ordenacion extends Actividad {

    private Boolean mostrarPuntuacion = false;
    private Boolean permitirReintento = false;
    private Boolean encontrarRespuestaMaestro = false;
    private Boolean encontrarRespuestaAlumno = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ordenacion_valores", joinColumns = @JoinColumn(name = "ordenacion_id"))
    @Column(name = "valor")
    @OrderColumn(name = "orden")
    private List<String> valores = new ArrayList<>();

    //Relaciones
    @OneToMany(mappedBy = "ordenacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RespAlumnoOrdenacion> respuestasAlumnoOrdenacion = new ArrayList<>();

    // Constructores
    public Ordenacion() {
        super();
    }

    public Ordenacion(String titulo, String descripcion, Integer puntuacion, String imagen,
                      Boolean respVisible, Integer posicion, Integer version, Tema tema, List<String> valores,
                      Boolean mostrarPuntuacion, Boolean permitirReintento, Boolean encontrarRespuestaMaestro, Boolean encontrarRespuestaAlumno) {
        super(titulo, descripcion, puntuacion, imagen, respVisible, posicion, version, tema     );
        this.valores = valores;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
       }

    // Getters y Setters
    public List<String> getValores() {
        return valores;
    }

    public void setValores(List<String> valores) {
        this.valores = valores;
    }

    public List<RespAlumnoOrdenacion> getRespuestasAlumnoOrdenacion() {
        return respuestasAlumnoOrdenacion;
    }

    public void setRespuestasAlumnoOrdenacion(List<RespAlumnoOrdenacion> respuestasAlumnoOrdenacion) {
        this.respuestasAlumnoOrdenacion = respuestasAlumnoOrdenacion;
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
        return "Ordenacion{" +
                "id=" + getId() +
                ", titulo='" + getTitulo() + '\'' +
                ", valores=" + valores +
                '}';
    }
}
