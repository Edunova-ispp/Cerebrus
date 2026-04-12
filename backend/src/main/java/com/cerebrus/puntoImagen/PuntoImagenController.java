package com.cerebrus.puntoImagen;

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

import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;

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
    public ResponseEntity<PuntoImagenDTO> encontrarPuntoImagenPorId(@PathVariable Long id) {
        return new ResponseEntity<>(obtenerPuntoImagenDTO(puntoImagenService.encontrarPuntoImagenPorId(id)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarPuntoImagenPorId(@PathVariable Long id) {
        puntoImagenService.eliminarPuntoImagenPorId(id);
        return ResponseEntity.noContent().build();
    }

    private static PuntoImagenDTO obtenerPuntoImagenDTO(PuntoImagen puntoImagen) {
        return new PuntoImagenDTO(
                puntoImagen.getId(),
                puntoImagen.getRespuesta(),
                puntoImagen.getPixelX(),
                puntoImagen.getPixelY()
        );
    }
}
