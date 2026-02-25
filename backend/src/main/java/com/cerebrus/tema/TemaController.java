package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
public class TemaController {

    private final TemaService temaService;

    @Autowired
    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Tema>> ObtenerTemasPorCursoAlumno(@PathVariable Integer cursoId) {
        return ResponseEntity.ok(temaService.ObtenerTemasPorCursoAlumno(cursoId));
    }
}
