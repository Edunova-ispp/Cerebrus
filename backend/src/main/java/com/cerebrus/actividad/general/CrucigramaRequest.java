package com.cerebrus.actividad.general;

import java.util.Map;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    @PositiveOrZero(message = "La puntuacion no puede ser negativa")
    private  Integer puntuacion;

    @NotNull
    private  Long temaId;

    @NotNull
    private  Boolean respVisible;

    @NotNull
    @Size(min = 1, max = 5, message = "El crucigrama debe tener entre 1 y 5 preguntas")
    private  Map<String,String> preguntasYRespuestas;


    public CrucigramaRequest() {}



}
