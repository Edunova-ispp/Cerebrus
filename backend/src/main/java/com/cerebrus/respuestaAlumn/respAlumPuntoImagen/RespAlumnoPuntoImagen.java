package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.puntoImage.PuntoImagen;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "resp_alumno_punto_imagen")
public class RespAlumnoPuntoImagen extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta es obligatoria")
    private String respuesta;

    //Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_imagen_id", referencedColumnName = "id", nullable=false)
    @NotNull(message = "El punto de imagen es obligatorio")
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
