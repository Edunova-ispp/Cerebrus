package com.cerebrus.actividad.tablero;


import java.util.LinkedHashMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableroRequest {

    @NotNull
    @NotBlank
    private  String titulo;

    private  String descripcion;

    @NotNull
    @PositiveOrZero(message = "La puntuacion no puede ser negativa")
    private  Integer puntuacion;

    @NotNull
    private  Boolean tamano; // Verdadero para 3x3, falso para 4x4

    @NotNull
    private  Long temaId;

    @NotNull
    private  Boolean respVisible;

    @NotNull
    @Size(min = 8, max = 15, message = "El tablero debe tener 8 o 15 preguntas")
    private  LinkedHashMap<String,String> preguntasYRespuestas;


    public TableroRequest() {}



}
