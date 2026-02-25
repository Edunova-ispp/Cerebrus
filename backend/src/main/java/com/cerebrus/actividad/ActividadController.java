package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    public ResponseEntity<Actividad> crearActividadTeoria(@RequestBody CrearActividadTeoriaRequest request, @RequestParam Long maestroId) {
        try {
            Actividad actividad = actividadService.crearActividadTeoria(request.getTitulo(), request.getDescripcion(), request.getPuntuacion(), request.getImagen(), request.getTemaId(), maestroId);
            return ResponseEntity.status(HttpStatus.CREATED).body(actividad);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class CrearActividadTeoriaRequest {
        private String titulo;
        private String descripcion;
        private Integer puntuacion;
        private String imagen;
        private Long temaId;

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Integer getPuntuacion() {
            return puntuacion;
        }

        public void setPuntuacion(Integer puntuacion) {
            this.puntuacion = puntuacion;
        }

        public String getImagen() {
            return imagen;
        }

        public void setImagen(String imagen) {
            this.imagen = imagen;
        }

        public Long getTemaId() {
            return temaId;
        }

        public void setTemaId(Long temaId) {
            this.temaId = temaId;
        }
    }
}
