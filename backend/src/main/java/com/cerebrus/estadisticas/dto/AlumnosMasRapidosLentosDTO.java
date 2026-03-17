package com.cerebrus.estadisticas.dto;

import java.util.List;

public class AlumnosMasRapidosLentosDTO {
    private List<TiempoAlumnoDTO> masRapidos;
    private List<TiempoAlumnoDTO> masLentos;
    private Double tiempoPromedio;

    public AlumnosMasRapidosLentosDTO() {
    }

    public AlumnosMasRapidosLentosDTO(List<TiempoAlumnoDTO> masRapidos, List<TiempoAlumnoDTO> masLentos, Double tiempoPromedio) {
        this.masRapidos = masRapidos;
        this.masLentos = masLentos;
        this.tiempoPromedio = tiempoPromedio;
    }

    public List<TiempoAlumnoDTO> getMasRapidos() {
        return masRapidos;
    }

    public void setMasRapidos(List<TiempoAlumnoDTO> masRapidos) {
        this.masRapidos = masRapidos;
    }

    public List<TiempoAlumnoDTO> getMasLentos() {
        return masLentos;
    }

    public void setMasLentos(List<TiempoAlumnoDTO> masLentos) {
        this.masLentos = masLentos;
    }

    public Double getTiempoPromedio() {
        return tiempoPromedio;
    }

    public void setTiempoPromedio(Double tiempoPromedio) {
        this.tiempoPromedio = tiempoPromedio;
    }

    @Override
    public String toString() {
        return "AlumnosMasRapidosLentosDTO{" +
                "masRapidos=" + masRapidos +
                ", masLentos=" + masLentos +
                ", tiempoPromedio=" + tiempoPromedio +
                '}';
    }
}
