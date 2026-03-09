package com.cerebrus.puntoimagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/puntos-imagen")
@CrossOrigin(origins = "*")
public class PuntoImagenController {

    private final PuntoImagenService puntoImagenService;

    @Autowired
    public PuntoImagenController(PuntoImagenService puntoImagenService) {
        this.puntoImagenService = puntoImagenService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PuntoImagen> obtenerPuntoImagenPorId(@PathVariable Long id) {
        return new ResponseEntity<>(puntoImagenService.obtenerPuntoImagenPorId(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarPuntoImagen(@PathVariable Long id) {
        puntoImagenService.eliminarPuntoImagen(id);
        return ResponseEntity.noContent().build();
    }
}
