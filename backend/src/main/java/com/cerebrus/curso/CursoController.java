package com.cerebrus.curso;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.Alumno;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
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

    @GetMapping("/{id}/puntos")
    public ResponseEntity<Map<Alumno, Integer>> obtenerPuntosCurso(@PathVariable Long id) {
        try {
            Curso curso = cursoService.getCursoById(id);
            Map<Alumno, Integer> puntosPorAlumno = cursoService.calcularTotalPuntosCursoPorAlumno(curso);
            return ResponseEntity.ok(puntosPorAlumno);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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


}
