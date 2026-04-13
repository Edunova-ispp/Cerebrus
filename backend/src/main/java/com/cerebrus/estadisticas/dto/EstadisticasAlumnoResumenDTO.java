package com.cerebrus.estadisticas.dto;

import java.util.List;

public class EstadisticasAlumnoResumenDTO {

    private Long alumnoId;
    private String nombreAlumno;
    private Double notaMedia;
    private Double notaMin;
    private Double notaMax;
    private Integer numActividadesCompletadas;
    private Integer totalActividades;
    private Integer tiempoTotalMinutos;
    private Integer tiempoTotalSegundos;
    private List<TemaEstadisticasAlumnoDTO> temas;

    public EstadisticasAlumnoResumenDTO() {}

    public EstadisticasAlumnoResumenDTO(Long alumnoId, String nombreAlumno, Double notaMedia,
            Double notaMin, Double notaMax, Integer numActividadesCompletadas,
            Integer totalActividades, Integer tiempoTotalMinutos, Integer tiempoTotalSegundos,
            List<TemaEstadisticasAlumnoDTO> temas) {
        this.alumnoId = alumnoId;
        this.nombreAlumno = nombreAlumno;
        this.notaMedia = notaMedia;
        this.notaMin = notaMin;
        this.notaMax = notaMax;
        this.numActividadesCompletadas = numActividadesCompletadas;
        this.totalActividades = totalActividades;
        this.tiempoTotalMinutos = tiempoTotalMinutos;
        this.tiempoTotalSegundos = tiempoTotalSegundos;
        this.temas = temas;
    }

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }
    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }
    public Double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(Double notaMedia) { this.notaMedia = notaMedia; }
    public Double getNotaMin() { return notaMin; }
    public void setNotaMin(Double notaMin) { this.notaMin = notaMin; }
    public Double getNotaMax() { return notaMax; }
    public void setNotaMax(Double notaMax) { this.notaMax = notaMax; }
    public Integer getNumActividadesCompletadas() { return numActividadesCompletadas; }
    public void setNumActividadesCompletadas(Integer numActividadesCompletadas) { this.numActividadesCompletadas = numActividadesCompletadas; }
    public Integer getTotalActividades() { return totalActividades; }
    public void setTotalActividades(Integer totalActividades) { this.totalActividades = totalActividades; }
    public Integer getTiempoTotalMinutos() { return tiempoTotalMinutos; }
    public void setTiempoTotalMinutos(Integer tiempoTotalMinutos) { this.tiempoTotalMinutos = tiempoTotalMinutos; }
    public Integer getTiempoTotalSegundos() { return tiempoTotalSegundos; }
    public void setTiempoTotalSegundos(Integer tiempoTotalSegundos) { this.tiempoTotalSegundos = tiempoTotalSegundos; }
    public List<TemaEstadisticasAlumnoDTO> getTemas() { return temas; }
    public void setTemas(List<TemaEstadisticasAlumnoDTO> temas) { this.temas = temas; }
}
