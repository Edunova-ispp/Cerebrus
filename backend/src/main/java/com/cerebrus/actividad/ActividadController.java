package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.general.dto.TeoriaDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/actividades")
@CrossOrigin(origins = "*")
@Validated
public class ActividadController {

    private final ActividadService actividadService;

    @Autowired
    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/{id}/maestro")
    public ResponseEntity<TeoriaDTO> getActividadMaestro(@PathVariable Long id) {
        try {
            Actividad actividad = actividadService.encontrarActividadPorIdMaestro(id);
            return ResponseEntity.ok(toTeoriaDto(actividad));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/alumno")
    public ResponseEntity<TeoriaDTO> getActividadAlumno(@PathVariable Long id) {
        try {
            Actividad actividad = actividadService.encontrarActividadPorIdAlumno(id);
            return ResponseEntity.ok(toTeoriaDto(actividad));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Long id) {
        try {
            actividadService.deleteActividad(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/teoria")
    public ResponseEntity<TeoriaDTO> crearActividadTeoria(@RequestBody @Valid CrearActividadTeoriaRequest request) {
        try {
            Actividad actividad = actividadService.crearActividadTeoria(
                request.getTitulo(),
                request.getDescripcion(),
                request.getImagen(),
                request.getTemaId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toTeoriaDto(actividad));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/teoria/{id}")
    public ResponseEntity<TeoriaDTO> updateActividadTeoria(@PathVariable Long id, @RequestBody @Valid CrearActividadTeoriaRequest request) {
        try {
            Actividad actividad = actividadService.updateActividadTeoria(
                id,
                request.getTitulo(),
                request.getDescripcion(),
                request.getImagen()
            );
            return ResponseEntity.ok(toTeoriaDto(actividad));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private static TeoriaDTO toTeoriaDto(Actividad actividad) {
        return new TeoriaDTO(
            actividad.getId(),
            actividad.getTitulo(),
            actividad.getDescripcion(),
            actividad.getImagen(),
            actividad.getPosicion(),
            actividad.getTema() == null ? null : actividad.getTema().getId()
        );
    }

    public static class CrearActividadTeoriaRequest {
        @NotBlank(message = "El titulo es obligatorio")
        private String titulo;
        private String descripcion;
        private Integer puntuacion;
        private String imagen;

        @NotNull(message = "El temaId es obligatorio")
        @Positive(message = "El temaId debe ser mayor que 0")
        private Long temaId;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getPuntuacion() { return puntuacion; }
        public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public Long getTemaId() { return temaId; }
        public void setTemaId(Long temaId) { this.temaId = temaId; }
    }

   
}