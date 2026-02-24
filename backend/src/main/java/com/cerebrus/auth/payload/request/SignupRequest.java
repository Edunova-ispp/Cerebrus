package com.cerebrus.auth.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

	@NotBlank
	private String nombre;

	@NotBlank
	private String primerApellido;

	@NotBlank
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
	

}
