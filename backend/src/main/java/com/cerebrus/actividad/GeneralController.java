package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    public record CrearTipoTestRequest(
        String titulo,
        String descripcion,
        Integer puntuacion,
        Long temaId,
        List<Long> preguntasId
    ) {}

    @PutMapping("/test")
    public ResponseEntity<General> crearTipoTest(@RequestBody CrearTipoTestRequest request) {
        General creado = generalService.crearTipoTest(
            request.titulo(),
            request.descripcion(),
            request.puntuacion(),
            request.temaId(),
            request.preguntasId()
        );

        return ResponseEntity.ok(creado);
    }
}
