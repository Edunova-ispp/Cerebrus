package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoServiceImpl cursoService;

    @Autowired
    public CursoController(CursoServiceImpl cursoService) {
        this.cursoService = cursoService;
    }

@GetMapping
    public ResponseEntity<List<Curso>> obtenerListadoCursos() {
        return ResponseEntity.ok(cursoService.ObtenerCursosUsuarioLogueado());
    }

    
@GetMapping("/{id}/detalles")
    public ResponseEntity<List<String>> obtenerDetallesCurso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cursoService.obtenerDetallesCurso(id));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("403 Forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }


}
