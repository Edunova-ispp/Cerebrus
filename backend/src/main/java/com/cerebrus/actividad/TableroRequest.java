package com.cerebrus.actividad;

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
public class TableroRequest {

    @NotNull
    private  String titulo;

    private  String descripcion;

    @NotNull
    private  Integer puntuacion;

    @NotNull
    private  Boolean tamano; // Verdadero para 3x3, falso para 4x4

    @NotNull
    private  Long temaId;

    @NotNull
    private  Boolean respVisible;

    @NotNull
    private  LinkedHashMap<String,String> preguntasYRespuestas;


    public TableroRequest() {}



}
