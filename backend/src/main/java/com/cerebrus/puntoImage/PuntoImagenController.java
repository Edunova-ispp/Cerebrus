package com.cerebrus.puntoImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.puntoImage.dto.PuntoImagenDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/puntos-imagen")
@CrossOrigin(origins = "*")
@Validated
public class PuntoImagenController {

    private final PuntoImagenService puntoImagenService;

    @Autowired
    public PuntoImagenController(PuntoImagenService puntoImagenService) {
        this.puntoImagenService = puntoImagenService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PuntoImagenDTO> obtenerPuntoImagenPorId(@PathVariable @NotNull Long id) {
        return new ResponseEntity<>(toPuntoImagenDTO(puntoImagenService.obtenerPuntoImagenPorId(id)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarPuntoImagen(@PathVariable @NotNull Long id) {
        puntoImagenService.eliminarPuntoImagen(id);
        return ResponseEntity.noContent().build();
    }

    private static PuntoImagenDTO toPuntoImagenDTO(PuntoImagen puntoImagen) {
        return new PuntoImagenDTO(
                puntoImagen.getId(),
                puntoImagen.getRespuesta(),
                puntoImagen.getPixelX(),
                puntoImagen.getPixelY()
        );
    }
}
