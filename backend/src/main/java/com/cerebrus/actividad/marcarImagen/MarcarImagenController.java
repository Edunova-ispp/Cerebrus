package com.cerebrus.actividad.marcarImagen;

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

import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;
import com.cerebrus.puntoimagen.dto.PuntoImagenDTO;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/marcar-imagenes")
@CrossOrigin(origins = "*")
public class MarcarImagenController {

    private final MarcarImagenService marcarImagenService;

    @Autowired
    public MarcarImagenController(MarcarImagenService marcarImagenService) {
        this.marcarImagenService = marcarImagenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MarcarImagenDTO> crearMarcarImagen(@RequestBody @Valid MarcarImagenDTO marcarImagenDTO) {
        MarcarImagen marcarImagenCreada = marcarImagenService.crearMarcarImagen(marcarImagenDTO);
        MarcarImagenDTO marcarImagenCreadaDTO = toMarcarImagenDTO(marcarImagenCreada);
        return new ResponseEntity<>(marcarImagenCreadaDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MarcarImagenDTO> obtenerMarcarImagenPorId(@PathVariable Long id) {
        MarcarImagen marcarImagen = marcarImagenService.obtenerMarcarImagenPorId(id);
        MarcarImagenDTO marcarImagenDTO = toMarcarImagenDTO(marcarImagen);
        return ResponseEntity.ok(marcarImagenDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MarcarImagenDTO> actualizarMarcarImagen(@PathVariable Long id, @RequestBody @Valid MarcarImagenDTO marcarImagenDTO) {
        MarcarImagen marcarImagenActualizada = marcarImagenService.actualizarMarcarImagen(id, marcarImagenDTO);
        MarcarImagenDTO marcarImagenActualizadaDTO = toMarcarImagenDTO(marcarImagenActualizada);
        return ResponseEntity.ok(marcarImagenActualizadaDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarMarcarImagen(@PathVariable Long id) {
        marcarImagenService.eliminarMarcarImagen(id);
        return ResponseEntity.noContent().build();
    }

    private static MarcarImagenDTO toMarcarImagenDTO(MarcarImagen marcarImagen) {
        return new MarcarImagenDTO(
            marcarImagen.getId(),
            marcarImagen.getTitulo(),
            marcarImagen.getDescripcion(),
            marcarImagen.getPuntuacion(),
            marcarImagen.getImagen(),
            marcarImagen.getRespVisible(),
            marcarImagen.getComentariosRespVisible(),
            marcarImagen.getTema().getId(),
            marcarImagen.getImagenAMarcar(),
            marcarImagen.getPuntosImagen().stream()
                .map(punto -> new PuntoImagenDTO(
                    punto.getId(),
                    punto.getRespuesta(),
                    punto.getPixelX(),
                    punto.getPixelY()
                ))
                .toList()
        );
    }
}
