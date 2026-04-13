package com.cerebrus.actividadAlumn.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActividadAlumnoDTO {

    private Long id;
    
    private Integer tiempo;

    private Integer puntuacion;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Integer nota;

    private Integer numAbandonos;

    private Boolean solucionUsada;

    private Long alumnoId;

    private Long actividadId;

    public ActividadAlumnoDTO(Long id, Integer tiempo, Integer puntuacion, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Boolean solucionUsada, Long alumnoId, Long actividadId) {
        this.id = id;
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.solucionUsada = solucionUsada;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }

    public ActividadAlumnoDTO(Long id, Integer puntuacion, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Boolean solucionUsada, Long alumnoId, Long actividadId) {
        this.id = id;
        this.puntuacion = puntuacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.solucionUsada = solucionUsada;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }

    public ActividadAlumnoDTO(Integer tiempo, Integer puntuacion, Integer nota, Integer numAbandonos, Boolean solucionUsada, Long alumnoId, Long actividadId) {
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
        this.solucionUsada = solucionUsada;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTiempo() {
        return tiempo;
    }

    public void setTiempo(Integer tiempo) {
        this.tiempo = tiempo;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
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

    public Boolean getSolucionUsada() {
        return solucionUsada;
    }

    public void setSolucionUsada(Boolean solucionUsada) {
        this.solucionUsada = solucionUsada;
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
}
