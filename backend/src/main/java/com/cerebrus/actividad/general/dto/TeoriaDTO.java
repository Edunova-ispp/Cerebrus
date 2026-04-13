package com.cerebrus.actividad.general.dto;

public class TeoriaDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final String imagen;
    private final Integer posicion;
    private final Long temaId;
    private final Boolean permitirReintento;

    public TeoriaDTO(Long id, String titulo, String descripcion, 
                     String imagen, Integer posicion, Long temaId, Boolean permitirReintento) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.posicion = posicion;
        this.temaId = temaId;
        this.permitirReintento = permitirReintento;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getImagen() { return imagen; }
    public Integer getPosicion() { return posicion; }
    public Long getTemaId() { return temaId; }
    public Boolean getPermitirReintento() { return permitirReintento; }
}