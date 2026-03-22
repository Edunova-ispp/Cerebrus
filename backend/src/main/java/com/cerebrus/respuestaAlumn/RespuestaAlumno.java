package com.cerebrus.respuestaAlumn;

import com.cerebrus.actividadAlumn.ActividadAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "respuesta_alumno")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RespuestaAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "El campo correcta es obligatorio")
    private Boolean correcta;

    //Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_alumno_id", nullable = false)
    @NotNull(message = "La actividad del alumno es obligatoria")
    private ActividadAlumno actividadAlumno;

    // Constructores
    public RespuestaAlumno() {
    }

    public RespuestaAlumno(Boolean correcta, ActividadAlumno actividadAlumno) {
        this.correcta = correcta;
        this.actividadAlumno = actividadAlumno;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCorrecta() {
        return correcta;
    }

    public void setCorrecta(Boolean correcta) {
        this.correcta = correcta;
    }

    public ActividadAlumno getActividadAlumno() {
        return actividadAlumno;
    }

    public void setActividadAlumno(ActividadAlumno actividadAlumno) {
        this.actividadAlumno = actividadAlumno;
    }

    @Override
    public String toString() {
        return "RespuestaAlumno{" +
                "id=" + id +
                ", correcta=" + correcta +
                '}';
    }
}
