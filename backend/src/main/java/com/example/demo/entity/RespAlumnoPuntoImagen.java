package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    // Constructores
    public RespAlumnoPuntoImagen() {
        super();
    }

    public RespAlumnoPuntoImagen(Boolean correcta, ActividadAlumno actividadAlumno,
                                 String respuesta, Integer pixelX, Integer pixelY) {
        super(correcta, actividadAlumno);
        this.respuesta = respuesta;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
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

    @Override
    public String toString() {
        return "RespAlumnoPuntoImagen{" +
                "id=" + getId() +
                ", correcta=" + getCorrecta() +
                ", respuesta='" + respuesta + '\'' +
                ", pixelX=" + pixelX +
                ", pixelY=" + pixelY +
                '}';
    }
}
