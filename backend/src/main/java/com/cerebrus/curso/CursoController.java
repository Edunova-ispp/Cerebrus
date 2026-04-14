package com.cerebrus.curso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/curso")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Curso> crearCurso(@RequestBody @Valid CrearCursoRequest request){
       
        try {
             Curso creado = cursoService.crearCurso(request.getTitulo(), request.getDescripcion(), request.getImagen(), request.getCodigo());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }  catch (RuntimeException e) {
           
            if (e.getMessage().equals("Este código ya esta en uso, por favor elige otro")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<String>> encontrarDetallesCursoPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cursoService.encontrarDetallesCursoPorId(id));
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

    @GetMapping
    public ResponseEntity<List<Curso>> encontrarCursosPorUsuarioLogueado() {
        return ResponseEntity.ok(cursoService.encontrarCursosPorUsuarioLogueado());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Curso> actualizarCurso(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarCursoRequest request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Curso cursoActualizado = cursoService.actualizarCurso(
                    id,
                    request.getTitulo(),
                    request.getDescripcion(),
                    request.getImagen(),
                    request.getCodigo()
            );
            return ResponseEntity.ok(cursoActualizado);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("404 Not Found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("403 Forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().equals("Este código ya es utilizado por otro curso, por favor elige otro")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarCursoPorId(@PathVariable Long id) {
        try {
            cursoService.eliminarCursoPorId(id);
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

    @GetMapping("/{id}/progreso")
    public ResponseEntity<ProgresoDTO> encontrarProgresoPorCursoId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cursoService.encontrarProgresoPorCursoId(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("404")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().contains("403")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PatchMapping("/{id}/visibilidad")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Curso> cambiarVisibilidad(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cursoService.cambiarVisibilidad(id));
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

    @GetMapping("/{id}/NotasMedias")
    public ResponseEntity<List<Double>> obtenerNotaMediaPorActividadPorCursoId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cursoService.obtenerNotaMediaPorActividadPorCursoId(id));
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

    public static class CrearCursoRequest {

        @NotBlank
        private String titulo;

        private String descripcion;
        private String imagen;
        private String codigo;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        
        public CrearCursoRequest() {}
    }

    public static class ActualizarCursoRequest {
        @NotBlank(message = "El título no puede estar vacío")
        private String titulo;

        private String descripcion;
        private String imagen;
        private String codigo;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public ActualizarCursoRequest() {}
    }
   
}
