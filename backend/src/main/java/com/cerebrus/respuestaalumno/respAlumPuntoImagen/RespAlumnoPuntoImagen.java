package com.cerebrus.respuestaAlumno.respAlumPuntoImagen;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.puntoimagen.PuntoImagen;
import com.cerebrus.respuestaAlumno.RespuestaAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_punto_imagen")
public class RespAlumnoPuntoImagen extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    //Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_imagen_id", referencedColumnName = "id", nullable=false)
    private PuntoImagen puntoImagen;

    // Constructores
    public RespAlumnoPuntoImagen() {
        super();
    }

    public RespAlumnoPuntoImagen(Boolean correcta, ActividadAlumno actividadAlumno,
                                 String respuesta, PuntoImagen puntoImagen) {
        super(correcta, actividadAlumno);
        this.respuesta = respuesta;
        this.puntoImagen = puntoImagen;
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public PuntoImagen getPuntoImagen() {
        return puntoImagen;
    }

    public void setPuntoImagen(PuntoImagen puntoImagen) {
        this.puntoImagen = puntoImagen;
    }

    @Override
    public String toString() {
        return "RespAlumnoPuntoImagen{" +
                "id=" + getId() +
                ", correcta=" + getCorrecta() +
                ", respuesta='" + respuesta + '\'' +
                ", puntoImagen=" + puntoImagen +
                '}';
    }
}
