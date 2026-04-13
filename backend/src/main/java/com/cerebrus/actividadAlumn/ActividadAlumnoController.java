package com.cerebrus.actividadAlumn;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.actividadAlumn.dto.ActividadAlumnoDTO;
import com.cerebrus.actividadAlumn.dto.CorreccionManualDTO;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/actividades-alumno")
@CrossOrigin(origins = "*")
public class ActividadAlumnoController {

    private final ActividadAlumnoService actividadAlumnoService;

    @Autowired
    public ActividadAlumnoController(ActividadAlumnoService actividadAlumnoService) {
        this.actividadAlumnoService = actividadAlumnoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ActividadAlumnoDTO> crearActAlumno(@RequestBody @Valid ActividadAlumnoDTO actividadAlumno) {
        ActividadAlumno actividadAlumnoCreada = actividadAlumnoService.crearActAlumno(
            actividadAlumno.getPuntuacion() == null ? 0 : actividadAlumno.getPuntuacion(),
            actividadAlumno.getFechaInicio() == null ? LocalDateTime.now() : actividadAlumno.getFechaInicio(),
            actividadAlumno.getFechaFin() == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : actividadAlumno.getFechaFin(),
            actividadAlumno.getNota() == null ? 0 : actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos() == null ? 0 : actividadAlumno.getNumAbandonos(),
            actividadAlumno.getAlumnoId(),
            actividadAlumno.getActividadId()
        );
        return new ResponseEntity<>(obtenerActAlumnoDto(actividadAlumnoCreada), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> encontrarActAlumnoPorId(@PathVariable Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoService.encontrarActAlumnoPorId(id);
        return new ResponseEntity<>(obtenerActAlumnoDto(actividadAlumno), HttpStatus.OK);
    }

    @GetMapping("/alumno/{alumnoId}/actividad/{actividadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> encontrarActAlumnoPorAlumnoIdYActId(
        @PathVariable Long alumnoId,
        @PathVariable Long actividadId
    ) {
        return actividadAlumnoService.encontrarActAlumnoPorAlumnoIdYActId(alumnoId, actividadId)
            .map(aa -> new ResponseEntity<>(obtenerActAlumnoDto(aa), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> actualizarActAlumno(@PathVariable Long id, @RequestBody @Valid ActividadAlumnoDTO actividadAlumno) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.actualizarActAlumno(
            id,
            actividadAlumno.getPuntuacion(),
            actividadAlumno.getFechaInicio(),
            actividadAlumno.getFechaFin(),
            actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos(),
            actividadAlumno.getSolucionUsada()
        );
        return new ResponseEntity<>(obtenerActAlumnoDto(actividadAlumnoActualizada), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarActAlumnoPorId(@PathVariable Long id) {
        actividadAlumnoService.eliminarActAlumnoPorId(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ensure/{actividadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> existeActAlumnoPorActIdYCurrentUserId(@PathVariable Long actividadId) {
        Integer exists = actividadAlumnoService.existeActAlumnoPorActIdYCurrentUserId(actividadId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/{id}/abandon")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> abandonarActAlumnoPorId(@PathVariable Long id) {
        ActividadAlumno updated = actividadAlumnoService.abandonarActAlumnoPorId(id);
        return new ResponseEntity<>(obtenerActAlumnoDto(updated), HttpStatus.OK);
    }

    
    @PutMapping("/corregir-manualmente/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActAlumnoManual(@PathVariable Long id, @RequestBody CorreccionManualDTO correccionManualDTO) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActAlumnoManual(id, correccionManualDTO.getNuevaNota(), correccionManualDTO.getNuevasCorreccionesRespuestasIds());
        return new ResponseEntity<>(obtenerActAlumnoDto(actividadAlumnoActualizada), HttpStatus.OK);
    }
    
    @PutMapping("/corregir-automaticamente/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActAlumnoAutomaticamente(
        @PathVariable Long id,
        @RequestBody(required = false) List<Long> respuestasIds
    ) {
        // cuando la petición no envía cuerpo el parámetro llega como null, el
        // servicio se encarga de recopilar los ids a partir de las respuestas
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActAlumnoAutomaticamente(id, respuestasIds);
        return new ResponseEntity<>(obtenerActAlumnoDto(actividadAlumnoActualizada), HttpStatus.OK);
    }
    
    @PutMapping("/corregir-automaticamente-general-clasificacion/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActAlumnoAutomaticamenteClasificacion(
        @PathVariable Long id,
        @RequestBody(required = false) List<Long> respuestasIds
    ) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActAlumnoAutomaticamenteClasificacion(id, respuestasIds);
        ActividadAlumnoDTO actividadAlumnoDTO = obtenerActAlumnoDto(actividadAlumnoActualizada);
        return new ResponseEntity<>(actividadAlumnoDTO, HttpStatus.OK);
    }

    private static ActividadAlumnoDTO obtenerActAlumnoDto(ActividadAlumno aa) {
        return new ActividadAlumnoDTO(
            aa.getId(),
            aa.getTiempoMinutos(),
            aa.getPuntuacion(),
            aa.getFechaInicio(),
            aa.getFechaFin(),
            aa.getNota(),
            aa.getNumAbandonos(),
            aa.getSolucionUsada(),
            aa.getAlumno() == null ? null : aa.getAlumno().getId(),
            aa.getActividad() == null ? null : aa.getActividad().getId()
        );
    }
}
