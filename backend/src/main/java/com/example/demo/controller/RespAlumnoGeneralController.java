package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RespAlumnoGeneralService;

@RestController
@RequestMapping("/api/respuestas-alumno-general")
@CrossOrigin(origins = "*")
public class RespAlumnoGeneralController {

    private final RespAlumnoGeneralService respAlumnoGeneralService;

    @Autowired
    public RespAlumnoGeneralController(RespAlumnoGeneralService respAlumnoGeneralService) {
        this.respAlumnoGeneralService = respAlumnoGeneralService;
    }
}
