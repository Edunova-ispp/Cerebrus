package com.cerebrus.actividadalumno;

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
    public ResponseEntity<ActividadAlumnoDTO> crearActividadAlumno(@RequestBody @Valid ActividadAlumnoDTO actividadAlumno) {
        ActividadAlumno actividadAlumnoCreada = actividadAlumnoService.crearActividadAlumno(
            actividadAlumno.getTiempo() == null ? 0 : actividadAlumno.getTiempo(),
            actividadAlumno.getPuntuacion() == null ? 0 : actividadAlumno.getPuntuacion(),
            actividadAlumno.getInicio() == null ? LocalDateTime.now() : actividadAlumno.getInicio(),
            actividadAlumno.getAcabada() == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : actividadAlumno.getAcabada(),
            actividadAlumno.getNota() == null ? 0 : actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos() == null ? 0 : actividadAlumno.getNumAbandonos(),
            actividadAlumno.getAlumnoId(),
            actividadAlumno.getActividadId()
        );
        return new ResponseEntity<>(toDto(actividadAlumnoCreada), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> readActividadAlumno(@PathVariable Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoService.readActividadAlumno(id);
        return new ResponseEntity<>(toDto(actividadAlumno), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> updateActividadAlumno(@PathVariable Long id, @RequestBody @Valid ActividadAlumnoDTO actividadAlumno) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.updateActividadAlumno(
            id,
            actividadAlumno.getTiempo(),
            actividadAlumno.getPuntuacion(),
            actividadAlumno.getInicio(),
            actividadAlumno.getAcabada(),
            actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos()
        );
        return new ResponseEntity<>(toDto(actividadAlumnoActualizada), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteActividadAlumno(@PathVariable Long id) {
        actividadAlumnoService.deleteActividadAlumno(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ensure/{actividadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> ensureActividadAlumno(@PathVariable Long actividadId) {
        Integer exists = actividadAlumnoService.ensureActividadAlumno(actividadId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/alumno/{alumnoId}/actividad/{actividadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> readActividadAlumnoByAlumnoIdAndActividadId(
        @PathVariable Long alumnoId,
        @PathVariable Long actividadId
    ) {
        return actividadAlumnoService.readActividadAlumnoByAlumnoIdAndActividadId(alumnoId, actividadId)
            .map(aa -> new ResponseEntity<>(toDto(aa), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{id}/abandon")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> abandonarActividadAlumno(@PathVariable Long id) {
        ActividadAlumno updated = actividadAlumnoService.abandonarActividadAlumno(id);
        return new ResponseEntity<>(toDto(updated), HttpStatus.OK);
    }

    private static ActividadAlumnoDTO toDto(ActividadAlumno aa) {
        return new ActividadAlumnoDTO(
            aa.getId(),
            aa.getTiempo(),
            aa.getPuntuacion(),
            aa.getInicio(),
            aa.getAcabada(),
            aa.getNota(),
            aa.getNumAbandonos(),
            aa.getAlumno() == null ? null : aa.getAlumno().getId(),
            aa.getActividad() == null ? null : aa.getActividad().getId()
        );
    }
  
    @PutMapping("/corregir-manualmente/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActividadAlumnoManual(@PathVariable Long id, @RequestBody CorreccionManualDTO correccionManualDTO) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActividadAlumnoManual(id, correccionManualDTO.getNuevaNota(), correccionManualDTO.getNuevasCorreccionesRespuestasIds());
        ActividadAlumnoDTO actividadAlumnoDTO = new ActividadAlumnoDTO(
            actividadAlumnoActualizada.getTiempo(),
            actividadAlumnoActualizada.getPuntuacion(),
            actividadAlumnoActualizada.getNota(),
            actividadAlumnoActualizada.getNumAbandonos(),
            actividadAlumnoActualizada.getAlumno().getId(),
            actividadAlumnoActualizada.getActividad().getId()
        );
        return new ResponseEntity<>(actividadAlumnoDTO, HttpStatus.OK);
    }

    @PutMapping("/corregir-automaticamente/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActividadAlumnoAutomaticamente(
        @PathVariable Long id,
        @RequestBody(required = false) List<Long> respuestasIds
    ) {
        // cuando la petición no envía cuerpo el parámetro llega como null, el
        // servicio se encarga de recopilar los ids a partir de las respuestas
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActividadAlumnoAutomaticamente(id, respuestasIds);
        ActividadAlumnoDTO actividadAlumnoDTO = new ActividadAlumnoDTO(
            actividadAlumnoActualizada.getTiempo(),
            actividadAlumnoActualizada.getPuntuacion(),
            actividadAlumnoActualizada.getNota(),
            actividadAlumnoActualizada.getNumAbandonos(),
            actividadAlumnoActualizada.getAlumno().getId(),
            actividadAlumnoActualizada.getActividad().getId()
        );
        return new ResponseEntity<>(actividadAlumnoDTO, HttpStatus.OK);
    }

        @PutMapping("/corregir-automaticamente-general-clasificacion/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumnoDTO> corregirActividadAlumnoAutomaticamenteGeneralClasificacion(
        @PathVariable Long id,
        @RequestBody(required = false) List<Long> respuestasIds
    ) {

       
        
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.corregirActividadAlumnoAutomaticamenteGeneralClasificacion(id, respuestasIds);
        ActividadAlumnoDTO actividadAlumnoDTO = new ActividadAlumnoDTO(
            actividadAlumnoActualizada.getTiempo(),
            actividadAlumnoActualizada.getPuntuacion(),
            actividadAlumnoActualizada.getNota(),
            actividadAlumnoActualizada.getNumAbandonos(),
            actividadAlumnoActualizada.getAlumno().getId(),
            actividadAlumnoActualizada.getActividad().getId()
        );
        return new ResponseEntity<>(actividadAlumnoDTO, HttpStatus.OK);
    }
}
