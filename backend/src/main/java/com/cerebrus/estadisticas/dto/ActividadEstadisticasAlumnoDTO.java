package com.cerebrus.estadisticas.dto;

import java.util.List;

public class ActividadEstadisticasAlumnoDTO {

    private Long actividadId;
    private String titulo;
    private String tipo;
    private Integer puntuacionMaxima;
    private Boolean completada;
    private Integer notaAlumno;
    private Integer puntuacionAlumno;
    private Double notaMediaClase;
    private Double desviacion;
    private List<IntentoActividadDTO> intentos;

    public ActividadEstadisticasAlumnoDTO() {}

    public ActividadEstadisticasAlumnoDTO(Long actividadId, String titulo, String tipo,
            Integer puntuacionMaxima, Boolean completada, Integer notaAlumno, Integer puntuacionAlumno,
            Double notaMediaClase, Double desviacion, List<IntentoActividadDTO> intentos) {
        this.actividadId = actividadId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.puntuacionMaxima = puntuacionMaxima;
        this.completada = completada;
        this.notaAlumno = notaAlumno;
        this.puntuacionAlumno = puntuacionAlumno;
        this.notaMediaClase = notaMediaClase;
        this.desviacion = desviacion;
        this.intentos = intentos;
    }

    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(Integer puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }
    public Boolean getCompletada() { return completada; }
    public void setCompletada(Boolean completada) { this.completada = completada; }
    public Integer getNotaAlumno() { return notaAlumno; }
    public void setNotaAlumno(Integer notaAlumno) { this.notaAlumno = notaAlumno; }
    public Integer getPuntuacionAlumno() { return puntuacionAlumno; }
    public void setPuntuacionAlumno(Integer puntuacionAlumno) { this.puntuacionAlumno = puntuacionAlumno; }
    public Double getNotaMediaClase() { return notaMediaClase; }
    public void setNotaMediaClase(Double notaMediaClase) { this.notaMediaClase = notaMediaClase; }
    public Double getDesviacion() { return desviacion; }
    public void setDesviacion(Double desviacion) { this.desviacion = desviacion; }
    public List<IntentoActividadDTO> getIntentos() { return intentos; }
    public void setIntentos(List<IntentoActividadDTO> intentos) { this.intentos = intentos; }
}
