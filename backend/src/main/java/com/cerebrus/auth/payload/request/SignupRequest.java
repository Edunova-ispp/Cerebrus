package com.cerebrus.auth.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String primerApellido;

    private String segundoApellido;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank 
    private String tipoUsuario;
    
    private String organizacion;
    
    private String nombreCentro;
    
    @PositiveOrZero(message = "Los puntos no pueden ser negativos")
    private Integer puntos;
}