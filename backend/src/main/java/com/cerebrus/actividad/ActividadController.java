package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/actividades")
@CrossOrigin(origins = "*")
public class ActividadController {

    private final ActividadService actividadService;

    @Autowired
    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @PostMapping("/teoria")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TeoriaDTO> crearActTeoria(@RequestBody @Valid CrearActTeoriaRequest request) {
            Actividad actividad = actividadService.crearActTeoria(
                request.getTitulo(),
                request.getDescripcion(),
                request.getImagen(),
                request.getTemaId(),
                request.getPermitirReintento()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(obtenerTeoriaDto(actividad));
    }

    @GetMapping("/{id}/alumno")
    public ResponseEntity<TeoriaDTO> encontrarActTeoriaPorId(@PathVariable Long id) {
        
        Actividad actividad = actividadService.encontrarActTeoriaPorId(id);
        return ResponseEntity.ok(obtenerTeoriaDto(actividad));
       
    }

    @GetMapping("/{id}/maestro")
    public ResponseEntity<TeoriaDTO> encontrarActTeoriaMaestroPorId(@PathVariable Long id) {
       
        Actividad actividad = actividadService.encontrarActTeoriaMaestroPorId(id);
        return ResponseEntity.ok(obtenerTeoriaDto(actividad));
       
    }

    @PutMapping("/teoria/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TeoriaDTO> actualizarActTeoria(
            @PathVariable Long id,
            @RequestBody @Valid CrearActTeoriaRequest request) {
            Actividad actividad = actividadService.actualizarActTeoria(
                id,
                request.getTitulo(),
                request.getDescripcion(),
                request.getImagen(),
                request.getPermitirReintento()
            );
            return ResponseEntity.ok(obtenerTeoriaDto(actividad));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarActTeoriaPorId(@PathVariable Long id) {
        
        actividadService.eliminarActTeoriaPorId(id);
        return ResponseEntity.noContent().build();
        
    }

    private static TeoriaDTO obtenerTeoriaDto(Actividad actividad) {
        return new TeoriaDTO(
            actividad.getId(),
            actividad.getTitulo(),
            actividad.getDescripcion(),
            actividad.getImagen(),
            actividad.getPosicion(),
            actividad.getTema() == null ? null : actividad.getTema().getId(),
            actividad.getPermitirReintento()
        );
    }

    public static class CrearActTeoriaRequest {
        @NotBlank
        private String titulo;
        private String descripcion;
        private Integer puntuacion;
        private String imagen;
        private Boolean permitirReintento;
        @NotNull
        private Long temaId;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getPuntuacion() { return puntuacion; }
        public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public Boolean getPermitirReintento() { return permitirReintento; }
        public void setPermitirReintento(Boolean permitirReintento) { this.permitirReintento = permitirReintento; }
        public Long getTemaId() { return temaId; }
        public void setTemaId(Long temaId) { this.temaId = temaId; }
    }

   
}