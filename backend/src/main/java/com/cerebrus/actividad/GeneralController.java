package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/generales")
@CrossOrigin(origins = "*")
public class GeneralController {

    private final GeneralService generalService;

    @Autowired
    public GeneralController(GeneralService generalService) {
        this.generalService = generalService;
    }

    public record TipoTestRequest(
        String titulo,
        String descripcion,
        Integer puntuacion,
        Long temaId,
        Boolean respVisible,
        String comentariosRespVisible,
        List<Long> preguntasId
    ) {}

    @PostMapping("/test")
    public ResponseEntity<General> crearTipoTest(@RequestBody TipoTestRequest request) {
        General creado = generalService.crearTipoTest(
            request.titulo(),
            request.descripcion(),
            request.puntuacion(),
            request.temaId(),
            request.respVisible(),
            request.comentariosRespVisible(),
            request.preguntasId()
        );

        return ResponseEntity.ok(creado);
    }

    @GetMapping("{id}")
    public ResponseEntity<General> readActividad(@PathVariable Long id){
        return ResponseEntity.ok(generalService.readActividad(id));
    }

    @PutMapping("test/update/{id}")
    public ResponseEntity<General> updateTipoTest(@PathVariable Long id, @RequestBody TipoTestRequest request){
        General actualizado = generalService.updateTipoTest(
            id,
            request.titulo(),
            request.descripcion(),
            request.puntuacion(),
            request.respVisible(),
            request.comentariosRespVisible(),
            request.preguntasId()
        );
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTipoTest(@PathVariable Long id) {
        generalService.deleteActividad(id);
        return ResponseEntity.noContent().build();
    }
}
