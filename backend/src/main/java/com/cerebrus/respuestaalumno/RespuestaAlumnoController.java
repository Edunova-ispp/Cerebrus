package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/respuestas-alumno")
@CrossOrigin(origins = "*")
public class RespuestaAlumnoController {

    private final RespuestaAlumnoService respuestaAlumnoService;

    @Autowired
    public RespuestaAlumnoController(RespuestaAlumnoService respuestaAlumnoService) {
        this.respuestaAlumnoService = respuestaAlumnoService;
    }

    @PutMapping("/cambiar-correcta/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespuestaAlumno> marcarODesmarcarRespuestaCorrecta(@PathVariable Long id) {
        RespuestaAlumno respuestaAlumnoActualizada = respuestaAlumnoService.marcarODesmarcarRespuestaCorrecta(id);
        return new ResponseEntity<>(respuestaAlumnoActualizada, HttpStatus.OK);
    }
}
