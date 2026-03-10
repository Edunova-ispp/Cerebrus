package com.cerebrus.actividad.DTO;

public class TeoriaDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final String imagen;
    private final Integer posicion;
    private final Long temaId;

    public TeoriaDTO(Long id, String titulo, String descripcion, Integer puntuacion,
                     String imagen, Integer posicion, Long temaId) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagen = imagen;
        this.posicion = posicion;
        this.temaId = temaId;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Integer getPuntuacion() { return puntuacion; }
    public String getImagen() { return imagen; }
    public Integer getPosicion() { return posicion; }
    public Long getTemaId() { return temaId; }
}