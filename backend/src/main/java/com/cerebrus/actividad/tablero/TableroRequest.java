package com.cerebrus.actividad.tablero;


import java.util.LinkedHashMap;

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
