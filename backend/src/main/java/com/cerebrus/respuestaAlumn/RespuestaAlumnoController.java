package com.cerebrus.respuestaAlumn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.respuestaAlumn.dto.RespuestasActividadDTO;


@RestController
@RequestMapping("/api/respuestas-alumno")
@CrossOrigin(origins = "*")
public class RespuestaAlumnoController {

    private final RespuestaAlumnoService respuestaAlumnoService;

    @Autowired
    public RespuestaAlumnoController(RespuestaAlumnoService respuestaAlumnoService) {
        this.respuestaAlumnoService = respuestaAlumnoService;
    }

    
    @GetMapping("/mis-respuestas/{actividadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespuestasActividadDTO> obtenerMisRespuestasActividad(
        @PathVariable Long actividadId
    ) {
        RespuestasActividadDTO respuestas = respuestaAlumnoService.obtenerRespuestasActividadAlumno(actividadId);
        return new ResponseEntity<>(respuestas, HttpStatus.OK);
    }
}
