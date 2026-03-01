package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadService;

@RestController
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
public class TemaController {

    private final TemaService temaService;
    private final ActividadService actividadService;

    @Autowired
    public TemaController(TemaService temaService, ActividadService actividadService) {
        this.temaService = temaService;
        this.actividadService = actividadService;
    }

    @PostMapping
    public ResponseEntity<Tema> crearTema(@RequestBody CrearTemaRequest request, @RequestParam Long maestroId) {
        try {
            Tema tema = temaService.crearTema(request.getTitulo(), request.getCursoId(), maestroId);
            return ResponseEntity.status(HttpStatus.CREATED).body(tema);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{temaId}")
    public ResponseEntity<Tema> renombrarTema(@PathVariable Long temaId, @RequestBody RenombrarTemaRequest request, @RequestParam Long maestroId) {
        try {
            Tema tema = temaService.renombrarTema(temaId, request.getNuevoTitulo(), maestroId);
            return ResponseEntity.ok(tema);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class CrearTemaRequest {
        private String titulo;
        private Long cursoId;

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public Long getCursoId() {
            return cursoId;
        }

        public void setCursoId(Long cursoId) {
            this.cursoId = cursoId;
        }
    }

    public static class RenombrarTemaRequest {
        private String nuevoTitulo;

        public String getNuevoTitulo() {
            return nuevoTitulo;
        }

        public void setNuevoTitulo(String nuevoTitulo) {
            this.nuevoTitulo = nuevoTitulo;
        }
    }

    @GetMapping("/curso/{cursoId}/alumno")
    public ResponseEntity<List<TemaDTO>> ObtenerTemasPorCursoAlumno(@PathVariable Long cursoId) {
        List<Tema> temas = temaService.ObtenerTemasPorCursoAlumno(cursoId);
        List<TemaDTO> temasDTO = temas.stream().map(tema -> {
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            return new TemaDTO(tema, actividades);
        }).toList();
        return ResponseEntity.ok(temasDTO);
    }

    @GetMapping("/curso/{cursoId}/maestro")
    public ResponseEntity<List<TemaDTO>> ObtenerTemasPorCursoMaestro(@PathVariable Long cursoId) {
        List<Tema> temas = temaService.ObtenerTemasPorCursoMaestro(cursoId);
        List<TemaDTO> temasDTO = temas.stream().map(tema -> {
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            return new TemaDTO(tema, actividades);
        }).toList();
        return ResponseEntity.ok(temasDTO);
    }
}

