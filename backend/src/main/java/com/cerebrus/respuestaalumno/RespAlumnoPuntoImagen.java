package com.cerebrus.respuestaalumno;

import com.cerebrus.actividad.MarcarImagen;
import com.cerebrus.actividadalumno.ActividadAlumno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resp_alumno_punto_imagen")
public class RespAlumnoPuntoImagen extends RespuestaAlumno {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    @Column(nullable = false)
    private Integer pixelX;

    @Column(nullable = false)
    private Integer pixelY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marcar_imagen_id", nullable = false)
    private MarcarImagen marcarImagen;

    // Constructores
    public RespAlumnoPuntoImagen() {
        super();
    }

    public RespAlumnoPuntoImagen(Boolean correcta, ActividadAlumno actividadAlumno,
                                 String respuesta, Integer pixelX, Integer pixelY, MarcarImagen marcarImagen) {
        super(correcta, actividadAlumno);
        this.respuesta = respuesta;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.marcarImagen = marcarImagen;
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Integer getPixelX() {
        return pixelX;
    }

    public void setPixelX(Integer pixelX) {
        this.pixelX = pixelX;
    }

    public Integer getPixelY() {
        return pixelY;
    }

    public void setPixelY(Integer pixelY) {
        this.pixelY = pixelY;
    }

    public MarcarImagen getMarcarImagen() {
        return marcarImagen;
    }

    public void setMarcarImagen(MarcarImagen marcarImagen) {
        this.marcarImagen = marcarImagen;
    }

    @Override
    public String toString() {
        return "RespAlumnoPuntoImagen{" +
                "id=" + getId() +
                ", correcta=" + getCorrecta() +
                ", respuesta='" + respuesta + '\'' +
                ", pixelX=" + pixelX +
                ", pixelY=" + pixelY +
                ", marcarImagen=" + marcarImagen +
                '}';
    }
}
