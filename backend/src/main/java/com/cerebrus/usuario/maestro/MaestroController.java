package com.cerebrus.usuario.maestro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.inscripcion.InscripcionService;
import com.cerebrus.inscripcion.dto.InscripcionRequestDTO;
import com.cerebrus.inscripcion.dto.InscripcionResponseDTO;
import com.cerebrus.usuario.alumno.AlumnoService;
import com.cerebrus.usuario.alumno.dto.AlumnosPageDTO;

@RestController
@RequestMapping("/api/maestros")
@CrossOrigin(origins = "*")
public class MaestroController {

    private static final Logger log = LoggerFactory.getLogger(MaestroController.class);

    private final MaestroService maestroService;
    private final AlumnoService alumnoService;
    private final InscripcionService inscripcionService;

    @Autowired
    public MaestroController(MaestroService maestroService, AlumnoService alumnoService, 
                             InscripcionService inscripcionService) {
        this.maestroService = maestroService;
        this.alumnoService = alumnoService;
        this.inscripcionService = inscripcionService;
    }

    
    @GetMapping("/alumnos")
    public ResponseEntity<AlumnosPageDTO> obtenerAlumnos(
            @RequestParam(value = "pagina", defaultValue = "0") int numeroPagina,
            @RequestParam(value = "tamanio", defaultValue = "10") int tamanioPagina) {
        
        
        AlumnosPageDTO respuesta = alumnoService.obtenerAlumnosDeOrganizacion(numeroPagina, tamanioPagina, null);
        
        
        return ResponseEntity.ok(respuesta);
    }

    
    @GetMapping("/alumnos-disponibles")
    public ResponseEntity<AlumnosPageDTO> obtenerAlumnosDisponiblesParaCurso(
            @RequestParam(value = "curso") Long cursoId,
            @RequestParam(value = "pagina", defaultValue = "0") int numeroPagina,
            @RequestParam(value = "tamanio", defaultValue = "10") int tamanioPagina) {
        
        
        AlumnosPageDTO respuesta = alumnoService.obtenerAlumnosNoInscritosEnCurso(numeroPagina, tamanioPagina, null, cursoId);
        
        
        return ResponseEntity.ok(respuesta);
    }

    
    @GetMapping("/alumnos/buscar")
    public ResponseEntity<AlumnosPageDTO> buscarAlumnos(
            @RequestParam(value = "q") String busqueda,
            @RequestParam(value = "pagina", defaultValue = "0") int numeroPagina,
            @RequestParam(value = "tamanio", defaultValue = "10") int tamanioPagina) {
        
        
        AlumnosPageDTO respuesta = alumnoService.obtenerAlumnosDeOrganizacion(numeroPagina, tamanioPagina, busqueda);
        
        
        return ResponseEntity.ok(respuesta);
    }

   
    @GetMapping("/alumnos-disponibles/buscar")
    public ResponseEntity<AlumnosPageDTO> buscarAlumnosDisponiblesParaCurso(
            @RequestParam(value = "curso") Long cursoId,
            @RequestParam(value = "q") String busqueda,
            @RequestParam(value = "pagina", defaultValue = "0") int numeroPagina,
            @RequestParam(value = "tamanio", defaultValue = "10") int tamanioPagina) {
        
        
        
        AlumnosPageDTO respuesta = alumnoService.obtenerAlumnosNoInscritosEnCurso(numeroPagina, tamanioPagina, busqueda, cursoId);
        
        
        return ResponseEntity.ok(respuesta);
    }

    
    @PostMapping("/inscribir-alumnos")
    public ResponseEntity<InscripcionResponseDTO> inscribirAlumnosEnCurso(
            @RequestParam(value = "curso") Long cursoId,
            @RequestBody InscripcionRequestDTO request) {
        
        
        InscripcionResponseDTO respuesta = inscripcionService.inscribirMultiplesAlumnos(cursoId, request);
        
        
        return ResponseEntity.ok(respuesta);
    }
}


