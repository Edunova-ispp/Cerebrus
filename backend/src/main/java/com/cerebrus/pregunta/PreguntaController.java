package com.cerebrus.pregunta;

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

import com.cerebrus.pregunta.dto.PreguntaRequest;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/preguntas")
@CrossOrigin(origins = "*")
public class PreguntaController {

    private final PreguntaService preguntaService;

    @Autowired
    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearPregunta(@RequestBody @Valid PreguntaRequest request) {
        Pregunta preguntaCreada = preguntaService.crearPregunta(
            request.getPregunta(),
            request.getImagen(),
            request.getActividadId()
        );
        return new ResponseEntity<>(preguntaCreada.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Pregunta> readPregunta(@PathVariable Long id) {
        Pregunta pregunta = preguntaService.readPregunta(id);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updatePregunta(@PathVariable Long id, @RequestBody @Valid Pregunta pregunta) {
        preguntaService.updatePregunta(
            id,
            pregunta.getPregunta(),
            pregunta.getImagen()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletePregunta(@PathVariable Long id) {
        preguntaService.deletePregunta(id);
        return ResponseEntity.noContent().build();
    }
}
