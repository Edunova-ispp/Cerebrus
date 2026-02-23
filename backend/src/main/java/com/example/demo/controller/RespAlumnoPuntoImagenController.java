package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RespAlumnoPuntoImagenService;

@RestController
@RequestMapping("/api/respuestas-alumno-punto-imagen")
@CrossOrigin(origins = "*")
public class RespAlumnoPuntoImagenController {

    private final RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @Autowired
    public RespAlumnoPuntoImagenController(RespAlumnoPuntoImagenService respAlumnoPuntoImagenService) {
        this.respAlumnoPuntoImagenService = respAlumnoPuntoImagenService;
    }
}
