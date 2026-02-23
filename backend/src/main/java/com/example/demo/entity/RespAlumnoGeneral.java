package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_general")
public class RespAlumnoGeneral extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    // Constructores
    public RespAlumnoGeneral() {
        super();
    }

    public RespAlumnoGeneral(Boolean correcta, ActividadAlumno actividadAlumno, String respuesta) {
        super(correcta, actividadAlumno);
        this.respuesta = respuesta;
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
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
