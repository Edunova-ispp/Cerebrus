package com.cerebrus.inscripcion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.inscripcion.dto.AlumnoCursoDTO;


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
            inscripcionService.crearInscripcion(codigoCurso);
            
            return ResponseEntity.ok("¡Alumno inscrito correctamente en el curso!");
            
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El curso no existe");
            } else if (e.getMessage().equals("400 Bad Request")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El alumno ya está inscrito en este curso");
            } else if (e.getMessage().equals("403 Forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes unirte a un curso no visible");
            } else if(e.getMessage().equals("401 Unauthorized")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
            }
        }
    }

    @GetMapping("/curso/{cursoId}/alumnos")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<List<AlumnoCursoDTO>> listarAlumnosPorCurso(@PathVariable Long cursoId) {
        try {
            List<AlumnoCursoDTO> dtos = inscripcionService.listarInscripcionesPorCurso(cursoId);
            return ResponseEntity.ok(dtos);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/curso/{cursoId}/alumnos/{alumnoId}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> expulsarAlumno(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            inscripcionService.expulsarAlumno(cursoId, alumnoId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}