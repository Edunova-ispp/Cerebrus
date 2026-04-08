package com.cerebrus.usuario.alumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "*")
public class AlumnoController {

    private final AlumnoService alumnoService;

    @Autowired
    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }


    @GetMapping("/mi-puntuacion-total")
    public ResponseEntity<Integer> obtenerMiPuntuacionTotal() {
        Integer totalPuntos = alumnoService.obtenerTotalPuntosAlumno();
        return ResponseEntity.ok(totalPuntos);
    }
}

