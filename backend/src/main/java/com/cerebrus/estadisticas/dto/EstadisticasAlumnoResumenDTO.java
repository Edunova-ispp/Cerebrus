package com.cerebrus.estadisticas.dto;

import java.util.List;

public class EstadisticasAlumnoResumenDTO {

    private Long alumnoId;
    private String nombreAlumno;
    private Double notaMedia;
    private Integer notaMin;
    private Integer notaMax;
    private Integer numActividadesCompletadas;
    private Integer totalActividades;
    private Integer tiempoTotalMinutos;
    private List<TemaEstadisticasAlumnoDTO> temas;

    public EstadisticasAlumnoResumenDTO() {}

    public EstadisticasAlumnoResumenDTO(Long alumnoId, String nombreAlumno, Double notaMedia,
            Integer notaMin, Integer notaMax, Integer numActividadesCompletadas,
            Integer totalActividades, Integer tiempoTotalMinutos,
            List<TemaEstadisticasAlumnoDTO> temas) {
        this.alumnoId = alumnoId;
        this.nombreAlumno = nombreAlumno;
        this.notaMedia = notaMedia;
        this.notaMin = notaMin;
        this.notaMax = notaMax;
        this.numActividadesCompletadas = numActividadesCompletadas;
        this.totalActividades = totalActividades;
        this.tiempoTotalMinutos = tiempoTotalMinutos;
        this.temas = temas;
    }

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }
    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }
    public Double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(Double notaMedia) { this.notaMedia = notaMedia; }
    public Integer getNotaMin() { return notaMin; }
    public void setNotaMin(Integer notaMin) { this.notaMin = notaMin; }
    public Integer getNotaMax() { return notaMax; }
    public void setNotaMax(Integer notaMax) { this.notaMax = notaMax; }
    public Integer getNumActividadesCompletadas() { return numActividadesCompletadas; }
    public void setNumActividadesCompletadas(Integer numActividadesCompletadas) { this.numActividadesCompletadas = numActividadesCompletadas; }
    public Integer getTotalActividades() { return totalActividades; }
    public void setTotalActividades(Integer totalActividades) { this.totalActividades = totalActividades; }
    public Integer getTiempoTotalMinutos() { return tiempoTotalMinutos; }
    public void setTiempoTotalMinutos(Integer tiempoTotalMinutos) { this.tiempoTotalMinutos = tiempoTotalMinutos; }
    public List<TemaEstadisticasAlumnoDTO> getTemas() { return temas; }
    public void setTemas(List<TemaEstadisticasAlumnoDTO> temas) { this.temas = temas; }
}
