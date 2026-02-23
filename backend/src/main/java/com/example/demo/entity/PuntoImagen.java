package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "punto_imagen")
public class PuntoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public PuntoImagen() {
    }

    public PuntoImagen(String respuesta, Integer pixelX, Integer pixelY, MarcarImagen marcarImagen) {
        this.respuesta = respuesta;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.marcarImagen = marcarImagen;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        return "PuntoImagen{" +
                "id=" + id +
                ", respuesta='" + respuesta + '\'' +
                ", pixelX=" + pixelX +
                ", pixelY=" + pixelY +
                '}';
    }
}
