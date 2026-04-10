package com.cerebrus.actividad.general.dto;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrucigramaRequest {

    @NotBlank
    private  String titulo;

    private  String descripcion;

    @NotNull
    @Min(1)
    private  Integer puntuacion;

    @NotNull
    private  Long temaId;

    @NotNull
    private  Boolean respVisible;

    @NotNull
    private  Map<String,String> preguntasYRespuestas;

    private Boolean mostrarPuntuacion;
    private Boolean permitirReintento;
    private Boolean encontrarRespuestaMaestro;
    private Boolean encontrarRespuestaAlumno;


    public CrucigramaRequest() {}



}
