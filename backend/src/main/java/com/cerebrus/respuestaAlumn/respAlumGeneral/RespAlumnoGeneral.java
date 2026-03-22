package com.cerebrus.respuestaAlumn.respAlumGeneral;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "resp_alumno_general")
public class RespAlumnoGeneral extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta es obligatoria")
    private String respuesta;

    //Relaciones
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @NotNull(message = "La pregunta es obligatoria")
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
