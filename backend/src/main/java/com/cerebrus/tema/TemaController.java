package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.cerebrus.actividad.dtoo.ActividadDTO;
import com.cerebrus.tema.dto.TemaDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TemaDTO> crearTema(@RequestBody @Valid CrearTemaRequest request, @RequestParam Long maestroId) {
        try {
            Tema tema = temaService.crearTema(request.getTitulo(), request.getCursoId(), maestroId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new TemaDTO(tema, List.of()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
    }

    @PutMapping("/{temaId}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<TemaDTO> renombrarTema(@PathVariable Long temaId, @RequestBody @Valid RenombrarTemaRequest request, @RequestParam Long maestroId) {
        try {
            Tema tema = temaService.renombrarTema(temaId, request.getNuevoTitulo(), maestroId);
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            List<ActividadDTO> actividadesDTO = actividades.stream().map(ActividadDTO::new).toList();
            return ResponseEntity.ok(new TemaDTO(tema, actividadesDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
    }

    public static class CrearTemaRequest {
        @NotBlank
        private String titulo;
        @NotNull
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
        @NotBlank
        private String nuevoTitulo;

        public String getNuevoTitulo() {
            return nuevoTitulo;
        }

        public void setNuevoTitulo(String nuevoTitulo) {
            this.nuevoTitulo = nuevoTitulo;
        }
    }

    @GetMapping("/{temaId}")
    public ResponseEntity<TemaDTO> obtenerTemaPorId(@PathVariable Long temaId) {
        
            Tema tema = temaService.obtenerTemaPorId(temaId);
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            List<ActividadDTO> actividadesDTO = actividades.stream().map(ActividadDTO::new).toList();
            return ResponseEntity.ok(new TemaDTO(tema, actividadesDTO));
        
    }

    @GetMapping("/curso/{cursoId}/alumno")
    public ResponseEntity<List<TemaDTO>> ObtenerTemasPorCursoAlumno(@PathVariable Long cursoId) {
        List<Tema> temas = temaService.ObtenerTemasPorCursoAlumno(cursoId);
        List<TemaDTO> temasDTO = temas.stream().map(tema -> {
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            List<ActividadDTO> actividadesDTO = actividades.stream().map(ActividadDTO::new).toList();
            return new TemaDTO(tema, actividadesDTO);
        }).toList();
        return ResponseEntity.ok(temasDTO);
    }

    @GetMapping("/curso/{cursoId}/maestro")
    public ResponseEntity<List<TemaDTO>> ObtenerTemasPorCursoMaestro(@PathVariable Long cursoId) {
        List<Tema> temas = temaService.ObtenerTemasPorCursoMaestro(cursoId);
        List<TemaDTO> temasDTO = temas.stream().map(tema -> {
            List<Actividad> actividades = actividadService.ObtenerActividadesPorTema(tema.getId());
            List<ActividadDTO> actividadesDTO = actividades.stream().map(ActividadDTO::new).toList();
            return new TemaDTO(tema, actividadesDTO);
        }).toList();
        return ResponseEntity.ok(temasDTO);
    }

    @DeleteMapping("/{temaId}")
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarTema(@PathVariable Long temaId) {
        
            temaService.eliminarTema(temaId);
            return ResponseEntity.noContent().build();
        
    }
}

