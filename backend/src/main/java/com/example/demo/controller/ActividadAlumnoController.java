package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ActividadAlumnoService;

@RestController
@RequestMapping("/api/actividades-alumno")
@CrossOrigin(origins = "*")
public class ActividadAlumnoController {

    private final ActividadAlumnoService actividadAlumnoService;

    @Autowired
    public ActividadAlumnoController(ActividadAlumnoService actividadAlumnoService) {
        this.actividadAlumnoService = actividadAlumnoService;
    }
}
