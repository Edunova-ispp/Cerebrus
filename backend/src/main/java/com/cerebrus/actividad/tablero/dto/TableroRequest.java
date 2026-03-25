package com.cerebrus.actividad.tablero.dto;


import java.util.LinkedHashMap;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableroRequest {

    @NotBlank
    private  String titulo;

    private  String descripcion;

    @NotNull
    @Min(1)
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
