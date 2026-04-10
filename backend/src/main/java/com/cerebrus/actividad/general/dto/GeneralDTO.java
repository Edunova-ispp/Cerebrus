package com.cerebrus.actividad.general.dto;

import com.cerebrus.comun.enumerados.TipoActGeneral;

public class GeneralDTO {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final Integer puntuacion;
    private final String imagen;
    private final Boolean respVisible;
    private final String comentariosRespVisible;
    private final Integer posicion;
    private final Integer version;
    private final Long temaId;
    private final TipoActGeneral tipo;
    private final Boolean mostrarPuntuacion;
    private final Boolean permitirReintento;
    private final Boolean encontrarRespuestaMaestro;
    private final Boolean encontrarRespuestaAlumno;

    public GeneralDTO(
            Long id,
            String titulo,
            String descripcion,
            Integer puntuacion,
            String imagen,
            Boolean respVisible,
            String comentariosRespVisible,
            Integer posicion,
            Integer version,
            Long temaId,
            TipoActGeneral tipo,
            Boolean mostrarPuntuacion,
            Boolean permitirReintento,
            Boolean encontrarRespuestaMaestro,
            Boolean encontrarRespuestaAlumno) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
        this.imagen = imagen;
        this.respVisible = respVisible;
        this.comentariosRespVisible = comentariosRespVisible;
        this.posicion = posicion;
        this.version = version;
        this.temaId = temaId;
        this.tipo = tipo;
        this.mostrarPuntuacion = mostrarPuntuacion;
        this.permitirReintento = permitirReintento;
        this.encontrarRespuestaMaestro = encontrarRespuestaMaestro;
        this.encontrarRespuestaAlumno = encontrarRespuestaAlumno;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public String getImagen() {
        return imagen;
    }

    public Boolean getRespVisible() {
        return respVisible;
    }

    public String getComentariosRespVisible() {
        return comentariosRespVisible;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public Integer getVersion() {
        return version;
    }

    public Long getTemaId() {
        return temaId;
    }

    public TipoActGeneral getTipo() {
        return tipo;
    }

    public void setId(long l) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setId'");
    }
}
