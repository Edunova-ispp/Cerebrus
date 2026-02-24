package com.cerebrus.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    
    @NotBlank 
    private String identificador;

    @NotBlank 
    private String password;
}
