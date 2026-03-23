package com.cerebrus.actividad.general;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrucigramaRequest {

    @NotNull
    private  String titulo;

    private  String descripcion;

    @NotNull
    private  Integer puntuacion;

    @NotNull
    private  Long temaId;

    @NotNull
    private  Boolean respVisible;

    @NotNull
    private  Map<String,String> preguntasYRespuestas;


    public CrucigramaRequest() {}



}
