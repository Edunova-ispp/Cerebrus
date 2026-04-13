package com.cerebrus.respuestaAlumn.respAlumGeneral;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_general")
public class RespAlumnoGeneral extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    @Column(nullable = false)
    private Integer numFallos = 0;

    //Relaciones
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    private Pregunta pregunta;

    // Constructores
    public RespAlumnoGeneral() {
        super();
    }

    public RespAlumnoGeneral(Boolean correcta, ActividadAlumno actividadAlumno, String respuesta, Pregunta pregunta) {
        super(correcta, actividadAlumno);
        this.respuesta = respuesta;
        this.pregunta = pregunta;
        this.numFallos = 0;
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Integer getNumFallos() {
        return numFallos;
    }

    public void setNumFallos(Integer numFallos) {
        this.numFallos = numFallos;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    @Override
    public String toString() {
        return "RespAlumnoGeneral{" +
                "id=" + getId() +
                ", correcta=" + getCorrecta() +
                ", numFallos=" + numFallos +
                ", respuesta='" + respuesta + '\'' +
                '}';
    }
}
