package com.cerebrus.respuestaAlumno.respAlumGeneral;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import com.cerebrus.exceptions.ResourceNotFoundException;

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
    public ResponseEntity<RespAlumnoGeneralCreateResponse> crearRespAlumnoGeneral(@RequestBody @Valid RespAlumnoGeneralRequest request) {
        RespAlumnoGeneralCreateResponse respAlumnoGeneralCreada = respAlumnoGeneralService.crearRespAlumnoGeneral(
    
            request.getActividadAlumnoId(),
            request.getRespuestaId(),
            request.getPreguntaId()
        );
        return new ResponseEntity<>(respAlumnoGeneralCreada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespAlumnoGeneral> readRespAlumnoGeneral(@PathVariable Long id) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralService.readRespAlumnoGeneral(id);
        return new ResponseEntity<>(respAlumnoGeneral, HttpStatus.OK);
    }

    // Este metodo toma como entrada un map con el id de la pregunta y la respuesta dada por el alumno, y devuelve un map con el id de la pregunta y si la respuesta es correcta o no
    // La nota final y la puntuacion de esta entrega es la entrada -1 en el HashMap
    @PostMapping("/crucigrama/{crucigramaId}")
    public ResponseEntity<HashMap<Long,String>> corregirCrucigrama(@RequestBody LinkedHashMap<Long, String> respuestas, @PathVariable Long crucigramaId) {
        try {
        if(respuestas.size() > 5 || respuestas.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();}
        HashMap<Long, String> resultado = respAlumnoGeneralService.corregirCrucigrama(respuestas, crucigramaId);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/abierta")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EvaluacionActividadAbiertaResponse> evaluarActividadAbierta(@RequestBody @Valid EvaluacionActividadAbiertaRequest request) {
        try {
            EvaluacionActividadAbiertaResponse respuesta = respAlumnoGeneralService.corregirActividadAbierta(request);
            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
