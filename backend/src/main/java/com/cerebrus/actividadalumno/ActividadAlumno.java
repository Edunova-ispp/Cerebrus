package com.cerebrus.actividadAlumno;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.respuestaAlumno.RespuestaAlumno;
import com.cerebrus.usuario.alumno.Alumno;

@Entity
@Table(name = "actividad_alumno")
public class ActividadAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column
    private LocalDateTime fechaInicio = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Column
    private LocalDateTime fechaFin = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Column(nullable = false)
    private Integer numAbandonos = 0;

    @Column (nullable = false)
    @Min(0)
    @Max(10)
    private Integer nota;
    
    //Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @OneToMany(mappedBy = "actividadAlumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();

    // Constructores
    public ActividadAlumno() {
    }

    public ActividadAlumno(Integer puntuacion, LocalDateTime fechaInicio, 
        LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Alumno alumno, Actividad actividad) {
        this.puntuacion = puntuacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.nota = nota;
        this.numAbandonos = numAbandonos;
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

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public LocalDateTime getFechaInicio(){
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime inicio){
        this.fechaInicio=inicio;
    }

    public LocalDateTime getFechaFin(){
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime acabada){
        this.fechaFin=acabada;
    }

    public Integer getNumAbandonos(){
        return numAbandonos;
    }

    public void setNumAbandonos(Integer numAbandonos){
        this.numAbandonos = numAbandonos;
    }

    public Integer getNota(){
        return nota;
    }

    public void setNota(Integer nota){
        this.nota = nota;
    }

    //Atributo derivado que calcula el tiempo tardado por el alumno en realizar una actividad concreta
    public Integer getTiempoMinutos() {
        LocalDateTime epoch = LocalDateTime.of(1970, 1, 1, 0, 0);
        if (fechaInicio == null || fechaFin == null) return 0;
        if (fechaInicio.equals(epoch) || fechaFin.equals(epoch)) return 0;
        if (fechaFin.isBefore(fechaInicio)) return 0;
        long minutos = ChronoUnit.MINUTES.between(fechaInicio, fechaFin);
        return (int) minutos;
    }

    public Integer getNumRepeticiones(){
        return respuestasAlumno == null ? 0 : respuestasAlumno.size();
    }

    public Integer getNumFallos(){
        Integer result;
        if (respuestasAlumno==null) {
            result=0;
        } else {
            result = (int) respuestasAlumno.stream().filter(r->Boolean.FALSE.equals(r.getCorrecta())).count();
        }
        return result;
    }

    public EstadoActividad getEstadoActividad(){
        LocalDateTime epoch = LocalDateTime.of(1970, 1, 1, 0, 0);
        // Actividad terminada si tiene fechaFin válida (no epoch) — cubre Teoría y cualquier
        // actividad que selle fechaFin al completarse sin generar respuestasAlumno
        if (fechaFin != null && !fechaFin.equals(epoch) && fechaFin.getYear() > 1970) {
            return EstadoActividad.TERMINADA;
        }
        if (getNumRepeticiones() > 0 && respuestasAlumno.get(respuestasAlumno.size()-1).getCorrecta().equals(Boolean.TRUE)) {
            return EstadoActividad.TERMINADA;
        }
        if (fechaInicio != null && !fechaInicio.equals(epoch)) {
            return EstadoActividad.EMPEZADA;
        }
        return EstadoActividad.SIN_EMPEZAR;
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
                ", tiempo=" + getTiempoMinutos() +
                ", puntuacion=" + puntuacion +
                ", fecha_inicio=" + fechaInicio + 
                ", fecha_fin=" + fechaFin +
                ", nota=" + nota +
                ", num_abandonos=" + numAbandonos +
                ", num_repeticiones=" + getNumRepeticiones() +
                ", num_fallos=" + getNumFallos() +
                ", estado=" + getEstadoActividad() +
                '}';
    }
}
