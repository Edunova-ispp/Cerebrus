package com.cerebrus.respuestaalumno;

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
@RequestMapping("/api/respuestas-alumno-general")
@CrossOrigin(origins = "*")
public class RespAlumnoGeneralController {

    private final RespAlumnoGeneralService respAlumnoGeneralService;

    @Autowired
    public RespAlumnoGeneralController(RespAlumnoGeneralService respAlumnoGeneralService) {
        this.respAlumnoGeneralService = respAlumnoGeneralService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RespAlumnoGeneralCreateResponse> crearRespAlumnoGeneral(@RequestBody @Valid RespAlumnoGeneral respAlumnoGeneral) {
        RespAlumnoGeneralCreateResponse respAlumnoGeneralCreada = respAlumnoGeneralService.crearRespAlumnoGeneral(
            respAlumnoGeneral.getActividadAlumno().getId(),
            respAlumnoGeneral.getRespuesta(),
            respAlumnoGeneral.getPregunta().getId()
        );
        return new ResponseEntity<>(respAlumnoGeneralCreada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespAlumnoGeneral> readRespAlumnoGeneral(@PathVariable Long id) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralService.readRespAlumnoGeneral(id);
        return new ResponseEntity<>(respAlumnoGeneral, HttpStatus.OK);
    }

// ESTOS MÉTODOS QUEDAN DEFINIDOS POR SI ES NECESARIO UTILIZARLOS, PERO PARA LA FEATURE 35 NO SON NECESARIOS

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespAlumnoGeneral> updateRespAlumnoGeneral(@PathVariable Long id, @RequestBody @Valid RespAlumnoGeneral respAlumnoGeneral) {
        RespAlumnoGeneral respAlumnoGeneralActualizada = respAlumnoGeneralService.updateRespAlumnoGeneral(
            id,
            respAlumnoGeneral.getCorrecta(),
            respAlumnoGeneral.getActividadAlumno().getId(),
            respAlumnoGeneral.getRespuesta(),
            respAlumnoGeneral.getPregunta().getId()
        );
        return new ResponseEntity<>(respAlumnoGeneralActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRespAlumnoGeneral(@PathVariable Long id) {
        respAlumnoGeneralService.deleteRespAlumnoGeneral(id);
        return ResponseEntity.noContent().build();
    }
}
