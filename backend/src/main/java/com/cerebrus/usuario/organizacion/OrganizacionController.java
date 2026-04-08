package com.cerebrus.usuario.organizacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.organizacion.dto.CreateUserRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/organizaciones")
@CrossOrigin(origins = "*")
public class OrganizacionController {

    private final OrganizacionService organizacionService;

    @Autowired
    public OrganizacionController(OrganizacionService organizacionService) {
        this.organizacionService = organizacionService;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<String> crearUsuario(@Valid @RequestBody CreateUserRequest request) {
    
            organizacionService.crearUsuario(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Usuario creado correctamente");
        
    }
}

