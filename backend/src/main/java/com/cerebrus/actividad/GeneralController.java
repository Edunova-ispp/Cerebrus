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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<General> crearActGeneral(@RequestBody @Valid General general) {
        
        General generalCreada = generalService.crearActGeneral(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible()
        );

        return new ResponseEntity<>(generalCreada, HttpStatus.CREATED);
    }

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearTipoTest(@RequestBody @Valid General general) {

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

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<GeneralTestDTO> readTipoTest(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoTest(id));
    }

    @GetMapping("/test/{id}/maestro")
    public ResponseEntity<GeneralTestMaestroDTO> readTipoTestMaestro(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoTestMaestro(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<General> readActividad(@PathVariable Long id){
        return ResponseEntity.ok(generalService.readActividad(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<General> updateActGeneral(@PathVariable Long id, @RequestBody @Valid General general){
        General actualizado = generalService.updateActGeneral(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId()
        );
        return ResponseEntity.ok(actualizado);
    }
    
    @PutMapping("/test/update/{id}")
    public ResponseEntity<General> updateTipoTest(@PathVariable Long id, @RequestBody @Valid General general){
        General actualizado = generalService.updateTipoTest(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPreguntas().stream().map(Pregunta::getId).toList(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId()
        );
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteActividad(@PathVariable Long id) {
        generalService.deleteActividad(id);
        return ResponseEntity.noContent().build();
    }

     @PostMapping("/clasificacion")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearTipoClasificacion(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();
        
        General generalCreada = generalService.crearGeneralClasificacion(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            preguntasId
        );

        return new ResponseEntity<>(generalCreada.getId(), HttpStatus.CREATED);
    }
    
    @GetMapping("/clasificacion/{id}/maestro")
    public ResponseEntity<GeneralClasificacionMaestroDTO> readTipoClasificacionMaestro(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoClasificacionMaestro(id));
    }

    @GetMapping("/clasificacion/{id}")
    public ResponseEntity<GeneralClasificacionDTO> readTipoClasificacion(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoClasificacion(id));
    }

    @PutMapping("/clasificacion/update/{id}")
    public ResponseEntity<GeneralClasificacionMaestroDTO> updateTipoClasificacion(@PathVariable Long id, @RequestBody @Valid General general){
        GeneralClasificacionMaestroDTO actualizado = generalService.updateTipoClasificacion(
            id,
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getRespVisible(),
            general.getComentariosRespVisible(),
            general.getPreguntas().stream().map(Pregunta::getId).toList(),
            general.getPosicion(),
            general.getVersion(),
            general.getTema().getId()
        );
        return ResponseEntity.ok(actualizado);
    }
}
