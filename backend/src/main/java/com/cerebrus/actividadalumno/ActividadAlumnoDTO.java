package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;

public class ActividadAlumnoDTO {

    private Long id;
    private Integer tiempo;
    private Integer puntuacion;
    private LocalDateTime inicio;
    private LocalDateTime acabada;
    private Integer nota;
    private Integer numAbandonos;
    private Long alumnoId;
    private Long actividadId;

    public ActividadAlumnoDTO() {
    }

    public ActividadAlumnoDTO(
        Long id,
        Integer tiempo,
        Integer puntuacion,
        LocalDateTime inicio,
        LocalDateTime acabada,
        Integer nota,
        Integer numAbandonos,
        Long alumnoId,
        Long actividadId
    ) {
        this.id = id;
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.inicio = inicio;
        this.acabada = acabada;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
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

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getAcabada() {
        return acabada;
    }

    public void setAcabada(LocalDateTime acabada) {
        this.acabada = acabada;
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
