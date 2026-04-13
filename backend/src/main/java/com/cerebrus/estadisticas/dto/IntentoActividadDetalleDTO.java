package com.cerebrus.estadisticas.dto;

import java.time.LocalDateTime;
import java.util.List;

public class IntentoActividadDetalleDTO {

    private Long intentoId;
    private Long cursoId;
    private Long alumnoId;
    private Long actividadId;
    private String actividadTitulo;
    private String actividadTipo;
    private String actividadImagen;
    private Integer puntuacionMaxima;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempoMinutos;
    private Integer puntuacion;
    private Integer nota;
    private Integer numAbandonos;
    private List<IntentoDetalleRespuestaDTO> respuestas;

    public IntentoActividadDetalleDTO() {}

    public IntentoActividadDetalleDTO(Long intentoId, Long cursoId, Long alumnoId, Long actividadId,
            String actividadTitulo, String actividadTipo, String actividadImagen, Integer puntuacionMaxima,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer tiempoMinutos,
            Integer puntuacion, Integer nota, Integer numAbandonos,
            List<IntentoDetalleRespuestaDTO> respuestas) {
        this.intentoId = intentoId;
        this.cursoId = cursoId;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
        this.actividadTitulo = actividadTitulo;
        this.actividadTipo = actividadTipo;
        this.actividadImagen = actividadImagen;
        this.puntuacionMaxima = puntuacionMaxima;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tiempoMinutos = tiempoMinutos;
        this.puntuacion = puntuacion;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.respuestas = respuestas;
    }

    public Long getIntentoId() {
        return intentoId;
    }

    public void setIntentoId(Long intentoId) {
        this.intentoId = intentoId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getActividadId() {
        return actividadId;
    }

    public void setActividadId(Long actividadId) {
        this.actividadId = actividadId;
    }

    public String getActividadTitulo() {
        return actividadTitulo;
    }

    public void setActividadTitulo(String actividadTitulo) {
        this.actividadTitulo = actividadTitulo;
    }

    public String getActividadTipo() {
        return actividadTipo;
    }

    public void setActividadTipo(String actividadTipo) {
        this.actividadTipo = actividadTipo;
    }

    public String getActividadImagen() {
        return actividadImagen;
    }

    public void setActividadImagen(String actividadImagen) {
        this.actividadImagen = actividadImagen;
    }

    public Integer getPuntuacionMaxima() {
        return puntuacionMaxima;
    }

    public void setPuntuacionMaxima(Integer puntuacionMaxima) {
        this.puntuacionMaxima = puntuacionMaxima;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTiempoMinutos() {
        return tiempoMinutos;
    }

    public void setTiempoMinutos(Integer tiempoMinutos) {
        this.tiempoMinutos = tiempoMinutos;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public Integer getNumAbandonos() {
        return numAbandonos;
    }

    public void setNumAbandonos(Integer numAbandonos) {
        this.numAbandonos = numAbandonos;
    }

    public List<IntentoDetalleRespuestaDTO> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<IntentoDetalleRespuestaDTO> respuestas) {
        this.respuestas = respuestas;
    }
}
