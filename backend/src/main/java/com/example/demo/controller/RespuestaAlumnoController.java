package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RespuestaAlumnoService;

@RestController
@RequestMapping("/api/respuestas-alumno")
@CrossOrigin(origins = "*")
public class RespuestaAlumnoController {

    private final RespuestaAlumnoService respuestaAlumnoService;

    @Autowired
    public RespuestaAlumnoController(RespuestaAlumnoService respuestaAlumnoService) {
        this.respuestaAlumnoService = respuestaAlumnoService;
    }
}
