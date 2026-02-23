package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RespAlumnoOrdenacionService;

@RestController
@RequestMapping("/api/respuestas-alumno-ordenacion")
@CrossOrigin(origins = "*")
public class RespAlumnoOrdenacionController {

    private final RespAlumnoOrdenacionService respAlumnoOrdenacionService;

    @Autowired
    public RespAlumnoOrdenacionController(RespAlumnoOrdenacionService respAlumnoOrdenacionService) {
        this.respAlumnoOrdenacionService = respAlumnoOrdenacionService;
    }
}
