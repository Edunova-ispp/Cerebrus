package com.cerebrus.inscripcion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.inscripcion.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "*")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @Autowired
    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping("/inscribe")
    public ResponseEntity<String> inscribirAlumno(@RequestParam String codigoCurso) {
        try {
            inscripcionService.CrearInscripcion(codigoCurso);
            
            return ResponseEntity.ok("¡Alumno inscrito correctamente en el curso!");
            
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El curso no existe");
            } else if (e.getMessage().equals("400 Bad Request")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El alumno ya está inscrito en este curso");
            } else if(e.getMessage().equals("401 Unauthorized")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
            }
        }
    }
}