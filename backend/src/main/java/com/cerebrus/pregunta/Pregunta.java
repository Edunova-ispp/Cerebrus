package com.cerebrus.pregunta;

import java.util.ArrayList;
import java.util.List;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;

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
@Table(name = "pregunta")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pregunta;

    private String imagen;

    //Relaciones
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RespuestaMaestro> respuestasMaestro = new ArrayList<>();

    @OneToMany(mappedBy = "pregunta", fetch = FetchType.LAZY)
    private List<RespAlumnoGeneral> respuestasAlumGeneral = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    // Constructores
    public Pregunta() {
    }

    public Pregunta(String pregunta, String imagen, Actividad actividad) {
        this.pregunta = pregunta;
        this.imagen = imagen;
        this.actividad = actividad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<RespuestaMaestro> getRespuestasMaestro() {
        return respuestasMaestro;
    }

    public void setRespuestasMaestro(List<RespuestaMaestro> respuestas) {
        this.respuestasMaestro = respuestas;
    }

    public List<RespAlumnoGeneral> getRespuestasAlumnoGeneral() {
        return respuestasAlumGeneral;
    }

    public void setRespuestasAlumnoGeneral(List<RespAlumnoGeneral> respuestasAlumno) {
        this.respuestasAlumGeneral = respuestasAlumno;
    }

    public Actividad getActividad(){
        return actividad;
    }

    public void setActividad(Actividad actividad){
        this.actividad = actividad;
    }


    @Override
    public String toString() {
        return "Pregunta{" +
                "id=" + id +
                ", pregunta='" + pregunta + '\'' +
                '}';
    }
}
