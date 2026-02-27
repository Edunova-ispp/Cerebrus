package com.cerebrus.actividadalumno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cglib.core.Local;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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

    @Column
    private LocalDate inicio = LocalDate.ofEpochDay(0);

    @Column
    private LocalDate acabada = LocalDate.ofEpochDay(0);

    @Column(nullable = false)
    private Integer numAbandonos = 0;

    @Column (nullable = false)
    @Min(0)
    @Max(10)
    private Integer nota;

    @Transient
    private Integer numRepeticiones;

    @Transient
    private Integer numFallos;

    @Transient
    private EstadoActividad estadoActividad;

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

    public Integer getNota(){
        return nota;
    }

    public void setNota(Integer nota){
        this.nota = nota;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getInicio(){
        return inicio;
    }

    public void setInicio(LocalDate inicio){
        this.inicio=inicio;
    }

    public LocalDate getAcabada(){
        return acabada;
    }

    public void setAcabada(LocalDate acabada){
        this.acabada=acabada;
    }

    public Integer getNumAbandonos(){
        return numAbandonos;
    }

    public void setNumAbandonos(Integer numAbandonos){
        this.numAbandonos = numAbandonos;
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
        EstadoActividad result;
        if(getNumRepeticiones() > 0 && respuestasAlumno.get(respuestasAlumno.size()).getCorrecta().equals(Boolean.TRUE)){
            result = EstadoActividad.TERMINADA;
        } else if (inicio != null && !(inicio.equals(LocalDate.ofEpochDay(0)))){
            result = EstadoActividad.EMPEZADA;
        } else {
            result = EstadoActividad.SIN_EMPEZAR;
        }
        return result;
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
                ", fecha_inicio=" + inicio + 
                ", fecha_fin=" + acabada +
                ", nota=" + nota +
                '}';
    }
}
