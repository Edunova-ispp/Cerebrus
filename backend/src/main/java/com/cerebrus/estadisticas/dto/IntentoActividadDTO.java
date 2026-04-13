package com.cerebrus.estadisticas.dto;

import java.time.LocalDateTime;

public class IntentoActividadDTO {

    private Long id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer puntuacion;
    private Integer nota;
    private Integer tiempoMinutos;
    private Integer tiempoSegundos;
    private Integer numAbandonos;

    public IntentoActividadDTO() {}

    public IntentoActividadDTO(Long id, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Integer puntuacion, Integer nota, Integer tiempoMinutos, Integer tiempoSegundos, Integer numAbandonos) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.puntuacion = puntuacion;
        this.nota = nota;
        this.tiempoMinutos = tiempoMinutos;
        this.tiempoSegundos = tiempoSegundos;
        this.numAbandonos = numAbandonos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Integer getPuntuacion() { return puntuacion; }
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }
    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }
    public Integer getTiempoMinutos() { return tiempoMinutos; }
    public void setTiempoMinutos(Integer tiempoMinutos) { this.tiempoMinutos = tiempoMinutos; }
    public Integer getTiempoSegundos() { return tiempoSegundos; }
    public void setTiempoSegundos(Integer tiempoSegundos) { this.tiempoSegundos = tiempoSegundos; }
    public Integer getNumAbandonos() { return numAbandonos; }
    public void setNumAbandonos(Integer numAbandonos) { this.numAbandonos = numAbandonos; }
}
