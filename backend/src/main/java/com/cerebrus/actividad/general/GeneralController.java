package com.cerebrus.actividad.general;

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

import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.CrucigramaRequest;
import com.cerebrus.actividad.general.dto.GeneralAbiertaAlumnoDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
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

    @GetMapping("/cartas/{id}")
    public ResponseEntity<GeneralCartaDTO> readTipoCarta(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoCarta(id));
    }

    @GetMapping("/cartas/{id}/maestro")
    public ResponseEntity<GeneralCartaMaestroDTO> readTipoCartaMaestro(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoCartaMaestro(id));
    }

    @PostMapping("/cartas/maestro")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearTipoCarta(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();

        General generalCreada = generalService.crearTipoCarta(
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

    @GetMapping("/{id}")
    public ResponseEntity<General> readActividad(@PathVariable Long id){
        return ResponseEntity.ok(generalService.readActividad(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateActGeneral(@PathVariable Long id, @RequestBody @Valid General general){
        generalService.updateActGeneral(
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
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/test/update/{id}")
    public ResponseEntity<GeneralTestDTO> updateTipoTest(@PathVariable Long id, @RequestBody @Valid General general){
        generalService.updateTipoTest(
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

        // Return a DTO to avoid lazy-loading serialization issues
        return ResponseEntity.ok(generalService.readTipoTest(id));
    }

    @PutMapping("/cartas/update/{id}")
    public ResponseEntity<GeneralCartaDTO> updateTipoCarta(@PathVariable Long id, @RequestBody @Valid General general) {

        generalService.updateTipoCarta(
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

        return ResponseEntity.ok(generalService.readTipoCarta(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteActividad(@PathVariable Long id) {
        generalService.deleteActividad(id);
        return ResponseEntity.noContent().build();
    }

     @PostMapping("/clasificacion")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearTipoClasificacion(@RequestBody @Valid General general) {

    
        
        General generalCreada = generalService.crearGeneralClasificacion(
            general.getTitulo(),
            general.getDescripcion(),
            general.getPuntuacion(),
            general.getTema().getId(),
            general.getRespVisible(),
            general.getComentariosRespVisible()
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

    @PostMapping("/crucigrama")
    public ResponseEntity<CrucigramaDTO> crearTipoCrucigrama(@RequestBody @Valid CrucigramaRequest crucigrama) {
        
        // Se ha decidido limitar el crucigrama a un maximo de 5 preguntas
        if(crucigrama.getPreguntasYRespuestas().size() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        CrucigramaDTO generalCreada = generalService.crearTipoCrucigrama(crucigrama);
        return ResponseEntity.ok(generalCreada);
        
        
    }

    @GetMapping("/crucigrama/{id}")
    public ResponseEntity<CrucigramaDTO> readTipoCrucigrama(@PathVariable Long id) {
            CrucigramaDTO crucigrama = generalService.readTipoCrucigrama(id);
            return ResponseEntity.ok(crucigrama);
    }

    @PutMapping("/crucigrama/{id}")
    public ResponseEntity<CrucigramaDTO> updateTipoCrucigrama(@PathVariable Long id, @RequestBody CrucigramaRequest crucigrama) {
    
        CrucigramaDTO updated = generalService.updateTipoCrucigrama(id, crucigrama);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/abierta/maestro")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> crearTipoAbierta(@RequestBody @Valid General general) {

        List<Long> preguntasId = general.getPreguntas().stream()
            .map(Pregunta::getId)
            .toList();

        General generalCreada = generalService.crearTipoAbierta(
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

    @GetMapping("/abierta/{id}")
    public ResponseEntity<GeneralAbiertaAlumnoDTO> readTipoAbierta(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoAbierta(id));
    }

    @GetMapping("/abierta/{id}/maestro")
    public ResponseEntity<GeneralAbiertaMaestroDTO> readTipoAbiertaMaestro(@PathVariable Long id) {
        return ResponseEntity.ok(generalService.readTipoAbiertaMaestro(id));
    }

    @PutMapping("/abierta/update/{id}")
    public ResponseEntity<GeneralAbiertaAlumnoDTO> updateTipoAbierta(@PathVariable Long id, @RequestBody @Valid General general) {

        generalService.updateTipoAbierta(
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

        return ResponseEntity.ok(generalService.readTipoAbierta(id));
    }

}
