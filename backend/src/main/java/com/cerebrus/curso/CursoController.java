package com.cerebrus.curso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.curso.dto.ProgresoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
        
            return ResponseEntity.ok(cursoService.obtenerDetallesCurso(id));
        
    }

    public static class CrearCursoRequest {

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
        
        public CrearCursoRequest() {
    }
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

    @PatchMapping("/{id}/visibilidad")
    public ResponseEntity<Curso> cambiarVisibilidad(@PathVariable Long id) {
        
            return ResponseEntity.ok(cursoService.cambiarVisibilidad(id));
        
    }

    @GetMapping("/{id}/progreso")
    public ResponseEntity<ProgresoDTO> obtenerProgreso(@PathVariable Long id) {
        
            return ResponseEntity.ok(cursoService.getProgreso(id));
       
    }

    public static class ActualizarCursoRequest {
        @NotBlank(message = "El título no puede estar vacío")
        private String titulo;

        private String descripcion;
        private String imagen;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }

        public ActualizarCursoRequest() {}
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Curso> actualizarCurso(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarCursoRequest request) {
        
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Curso cursoActualizado = cursoService.actualizarCurso(
                    id,
                    request.getTitulo(),
                    request.getDescripcion(),
                    request.getImagen()
            );
            return ResponseEntity.ok(cursoActualizado);
        
    }
    @GetMapping("/{id}/NotasMedias")
    public ResponseEntity<List<Integer>> obtenerNotasMedias(@PathVariable Long id) {
       
            return ResponseEntity.ok(cursoService.getNotaMediaPorActividad(id));
        
    }


    @DeleteMapping("/{id}")
public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
    
        cursoService.eliminarCurso(id);
        return ResponseEntity.noContent().build();
    
}

   
}
