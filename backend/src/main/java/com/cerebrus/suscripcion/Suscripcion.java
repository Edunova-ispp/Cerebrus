package com.cerebrus.suscripcion;

import java.time.LocalDate;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.usuario.organizacion.Organizacion;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numMaestros;

    @Column(nullable = false)
    private Integer numAlumnos;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    private EstadoPagoSuscripcion estadoPago;
    
    private String transaccionId;

    //Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    // Constructores
    public Suscripcion() {
    }

    public Suscripcion(Integer numMaestros, Integer numAlumnos, Double precio,
                       LocalDate fechaInicio, LocalDate fechaFin, Organizacion organizacion, EstadoPagoSuscripcion estadoPago, String transaccionId) {
        this.numMaestros = numMaestros;
        this.numAlumnos = numAlumnos;
        this.precio = precio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.organizacion = organizacion;
        this.estadoPago = estadoPago;
        this.transaccionId = transaccionId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumMaestros() {
        return numMaestros;
    }

    public void setNumMaestros(Integer numMaestros) {
        this.numMaestros = numMaestros;
    }

    public Integer getNumAlumnos() {
        return numAlumnos;
    }

    public void setNumAlumnos(Integer numAlumnos) {
        this.numAlumnos = numAlumnos;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoPagoSuscripcion getEstadoPagoSuscripcion(){
        return estadoPago;
    }

    public void setEstadoPagoSuscripcion(EstadoPagoSuscripcion estadoPago){
        this.estadoPago = estadoPago;
    }

    public String getTransaccionId(){
        return transaccionId;
    }

    public void setTransaccionId(String transaccionId){
        this.transaccionId = transaccionId;
    }

    //Atributo derivado
    public boolean isActiva() {
    LocalDate hoy = LocalDate.now();
    return (hoy.isAfter(fechaInicio) || hoy.isEqual(fechaInicio)) 
            && (hoy.isBefore(fechaFin) || hoy.isEqual(fechaFin)) 
            && (this.estadoPago == EstadoPagoSuscripcion.PAGADA);
}

    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    @Override
    public String toString() {
        return "Suscripcion{" +
                "id=" + id +
                ", numMaestros=" + numMaestros +
                ", numAlumnos=" + numAlumnos +
                ", precio=" + precio +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }
}
