package com.cerebrus.inscripcion.dto;

import java.time.LocalDate;

public class AlumnoCursoDTO {

    private Long alumnoId;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String nombreUsuario;
    private String correoElectronico;
    private Integer puntos;
    private LocalDate fechaInscripcion;

    public AlumnoCursoDTO(Long alumnoId, String nombre, String primerApellido, String segundoApellido,
                          String nombreUsuario, String correoElectronico, Integer puntos, LocalDate fechaInscripcion) {
        this.alumnoId = alumnoId;
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.nombreUsuario = nombreUsuario;
        this.correoElectronico = correoElectronico;
        this.puntos = puntos;
        this.fechaInscripcion = fechaInscripcion;
    }

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }

    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
}
