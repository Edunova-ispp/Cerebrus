package com.cerebrus.actividadalumno;

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
    public ResponseEntity<ActividadAlumno> crearActividadAlumno(@RequestBody @Valid ActividadAlumno actividadAlumno) {
        ActividadAlumno actividadAlumnoCreada = actividadAlumnoService.crearActividadAlumno(
            actividadAlumno.getTiempo(),
            actividadAlumno.getPuntuacion(),
            actividadAlumno.getFecha(),
            actividadAlumno.getInicio(),
            actividadAlumno.getAcabada(),
            actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos(),
            actividadAlumno.getAlumno().getId(),
            actividadAlumno.getActividad().getId()
        );
        return new ResponseEntity<>(actividadAlumnoCreada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumno> readActividadAlumno(@PathVariable Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoService.readActividadAlumno(id);
        return new ResponseEntity<>(actividadAlumno, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActividadAlumno> updateActividadAlumno(@PathVariable Long id, @RequestBody @Valid ActividadAlumno actividadAlumno) {
        ActividadAlumno actividadAlumnoActualizada = actividadAlumnoService.updateActividadAlumno(
            id,
            actividadAlumno.getTiempo(),
            actividadAlumno.getPuntuacion(),
            actividadAlumno.getFecha(),
            actividadAlumno.getInicio(),
            actividadAlumno.getAcabada(),
            actividadAlumno.getNota(),
            actividadAlumno.getNumAbandonos()
        );
        return new ResponseEntity<>(actividadAlumnoActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteActividadAlumno(@PathVariable Long id) {
        actividadAlumnoService.deleteActividadAlumno(id);
        return ResponseEntity.noContent().build();
    }
}
