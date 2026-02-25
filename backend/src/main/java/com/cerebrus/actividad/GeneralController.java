package com.cerebrus.actividad;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.pregunta.Pregunta;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/generales")
@CrossOrigin(origins = "*")
public class GeneralController {

    private final GeneralService generalService;

    @Autowired
    public GeneralController(GeneralService generalService) {
        this.generalService = generalService;
    }

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<General> crearTipoTest(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();
        
        General generalCreada = generalService.crearTipoTest(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            preguntasId
        );

        return new ResponseEntity<>(generalCreada, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<General> readActividad(@PathVariable Long id){
        return ResponseEntity.ok(generalService.readActividad(id));
    }

    @PutMapping("test/update/{id}")
    public ResponseEntity<General> updateTipoTest(@PathVariable Long id, @RequestBody @Valid General general){
        General actualizado = generalService.updateTipoTest(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPreguntas().stream().map(Pregunta::getId).toList()
        );
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteActividad(@PathVariable Long id) {
        generalService.deleteActividad(id);
        return ResponseEntity.noContent().build();
    }
}
