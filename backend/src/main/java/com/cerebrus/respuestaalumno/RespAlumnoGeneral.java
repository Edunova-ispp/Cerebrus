package com.cerebrus.respuestaalumno;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.pregunta.Pregunta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_general")
public class RespAlumnoGeneral extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

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
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
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
                ", respuesta='" + respuesta + '\'' +
                '}';
    }
}
