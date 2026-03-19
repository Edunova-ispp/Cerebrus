package com.cerebrus.estadisticas.dto;

public class TiempoAlumnoDTO {
    private String nombreAlumno;
    private Long alumnoId;
    private Integer tiempoMinutos;

    public TiempoAlumnoDTO() {
    }

    public TiempoAlumnoDTO(String nombreAlumno, Long alumnoId, Integer tiempoMinutos) {
        this.nombreAlumno = nombreAlumno;
        this.alumnoId = alumnoId;
        this.tiempoMinutos = tiempoMinutos;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Integer getTiempoMinutos() {
        return tiempoMinutos;
    }

    public void setTiempoMinutos(Integer tiempoMinutos) {
        this.tiempoMinutos = tiempoMinutos;
    }

    @Override
    public String toString() {
        return "TiempoAlumnoDTO{" +
                "nombreAlumno='" + nombreAlumno + '\'' +
                ", alumnoId=" + alumnoId +
                ", tiempoMinutos=" + tiempoMinutos +
                '}';
    }
}
