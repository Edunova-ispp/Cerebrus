package com.cerebrus.respuestaMaestro;

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

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/respuestas")
@CrossOrigin(origins = "*")
public class RespuestaMaestroController {

    private final RespuestaMaestroService respuestaService;

    @Autowired
    public RespuestaMaestroController(RespuestaMaestroService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Long> crearRespuestaMaestro(@RequestBody @Valid RespuestaMaestro respuesta) {
        RespuestaMaestro respuestaCreada = respuestaService.crearRespuestaMaestro(
            respuesta.getRespuesta(),
            respuesta.getImagen(),
            respuesta.getCorrecta(),
            respuesta.getPregunta().getId()
        );
        return new ResponseEntity<>(respuestaCreada.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RespuestaMaestro> encontrarRespuestaMaestroPorId(@PathVariable Long id) {
        RespuestaMaestro respuesta = respuestaService.encontrarRespuestaMaestroPorId(id);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> actualizarRespuestaMaestro(@PathVariable Long id, @RequestBody @Valid RespuestaMaestro respuesta) {
        respuestaService.actualizarRespuestaMaestro(
            id,
            respuesta.getRespuesta(),
            respuesta.getImagen(),
            respuesta.getCorrecta()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAESTRO')")
    public ResponseEntity<Void> eliminarRespuestaMaestroPorId(@PathVariable Long id) {
        respuestaService.eliminarRespuestaMaestroPorId(id);
        return ResponseEntity.noContent().build();
    }
}
