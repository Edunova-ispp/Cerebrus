package com.cerebrus.actividad.DTO;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.cerebrus.pregunta.PreguntaDTO;
import com.cerebrus.pregunta.PreguntaRequest;

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
    private  LinkedHashMap<String,String> preguntasYRespuestas;


    public CrucigramaRequest() {}



}
