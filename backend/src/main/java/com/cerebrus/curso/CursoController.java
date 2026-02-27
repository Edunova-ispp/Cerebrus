package com.cerebrus.curso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoServiceImpl cursoService;

    @Autowired
    public CursoController(CursoServiceImpl cursoService) {
        this.cursoService = cursoService;
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

    public class CrearCursoRequest {

        @NotBlank
        private String titulo;

        private String descripcion;
        private String imagen;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
    }

    @PostMapping("/curso")
    public ResponseEntity<Curso> crearCurso(@RequestBody @Valid CrearCursoRequest request){
        Curso creado = cursoService.crearCurso(request.getTitulo(), request.getDescripcion(), request.getImagen());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Curso>> obtenerCursos() {
        return ResponseEntity.ok(cursoService.ObtenerCursosUsuarioLogueado());
    }
}
