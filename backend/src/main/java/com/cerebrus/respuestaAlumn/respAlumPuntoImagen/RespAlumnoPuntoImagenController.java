package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.dto.RespAlumnoPuntoImagenDTO;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/respuestas-alumno-punto-imagen")
@CrossOrigin(origins = "*")
public class RespAlumnoPuntoImagenController {

    private final RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @Autowired
    public RespAlumnoPuntoImagenController(RespAlumnoPuntoImagenService respAlumnoPuntoImagenService) {
        this.respAlumnoPuntoImagenService = respAlumnoPuntoImagenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RespAlumnoPuntoImagenDTO> crearRespAlumnoPuntoImagen(@RequestBody @Valid RespAlumnoPuntoImagenDTO respAlumnoPuntoImagenDTO) {
        RespAlumnoPuntoImagen respAlumnoPuntoImagen = respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen(
            respAlumnoPuntoImagenDTO.getRespuesta(),
            respAlumnoPuntoImagenDTO.getPuntoImagenId(),
            respAlumnoPuntoImagenDTO.getActividadAlumnoId()
        );
        return new ResponseEntity<>(toDto(respAlumnoPuntoImagen), HttpStatus.CREATED);
    }

    private static RespAlumnoPuntoImagenDTO toDto(RespAlumnoPuntoImagen respAlumnoPuntoImagen) {
        return new RespAlumnoPuntoImagenDTO(
            respAlumnoPuntoImagen.getId(),
            respAlumnoPuntoImagen.getRespuesta(),
            respAlumnoPuntoImagen.getPuntoImagen().getId(),
            respAlumnoPuntoImagen.getActividadAlumno().getId()
        );
    }

}
