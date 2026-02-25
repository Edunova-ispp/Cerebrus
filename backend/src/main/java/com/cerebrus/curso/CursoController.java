package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoService cursoService;

    @Autowired
    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

@GetMapping
    public ResponseEntity<List<Curso>> obtenerListadoCursos() {
        return ResponseEntity.ok(cursoService.ObtenerCursosUsuarioLogueado());
    }


}
