package com.cerebrus.actividadalumno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.respuestaalumno.RespuestaAlumno;

@Entity
@Table(name = "actividad_alumno")
public class ActividadAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tiempo;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @OneToMany(mappedBy = "actividadAlumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();

    // Constructores
    public ActividadAlumno() {
    }

    public ActividadAlumno(Integer tiempo, Integer puntuacion, LocalDate fecha,
                           Alumno alumno, Actividad actividad) {
        this.tiempo = tiempo;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
        this.alumno = alumno;
        this.actividad = actividad;
    }

    // Getters y Setters
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public List<RespuestaAlumno> getRespuestasAlumno() {
        return respuestasAlumno;
    }

    public void setRespuestasAlumno(List<RespuestaAlumno> respuestasAlumno) {
        this.respuestasAlumno = respuestasAlumno;
    }

    @Override
    public String toString() {
        return "ActividadAlumno{" +
                "id=" + id +
                ", tiempo=" + tiempo +
                ", puntuacion=" + puntuacion +
                ", fecha=" + fecha +
                '}';
    }
}
