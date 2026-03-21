package com.cerebrus.estadisticas.dto;

import java.util.List;

public class TemaEstadisticasAlumnoDTO {

    private Long temaId;
    private String titulo;
    private Boolean completado;
    private List<ActividadEstadisticasAlumnoDTO> actividades;

    public TemaEstadisticasAlumnoDTO() {}

    public TemaEstadisticasAlumnoDTO(Long temaId, String titulo, Boolean completado,
            List<ActividadEstadisticasAlumnoDTO> actividades) {
        this.temaId = temaId;
        this.titulo = titulo;
        this.completado = completado;
        this.actividades = actividades;
    }

    public Long getTemaId() { return temaId; }
    public void setTemaId(Long temaId) { this.temaId = temaId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }
    public List<ActividadEstadisticasAlumnoDTO> getActividades() { return actividades; }
    public void setActividades(List<ActividadEstadisticasAlumnoDTO> actividades) { this.actividades = actividades; }
}
