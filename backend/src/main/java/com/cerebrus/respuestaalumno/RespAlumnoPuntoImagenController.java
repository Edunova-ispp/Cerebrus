package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


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
    public ResponseEntity<RespAlumnoPuntoImagenDTO> crearRespAlumnoPuntoImagen(RespAlumnoPuntoImagenDTO respAlumnoPuntoImagenDTO) {
        RespAlumnoPuntoImagen respAlumnoPuntoImagen = respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen(
            respAlumnoPuntoImagenDTO.getRespuesta(),
            respAlumnoPuntoImagenDTO.getPixelX(),
            respAlumnoPuntoImagenDTO.getPixelY(),
            respAlumnoPuntoImagenDTO.getMarcarImagenId(),
            respAlumnoPuntoImagenDTO.getActividadAlumnoId()
        );
        return new ResponseEntity<>(toDto(respAlumnoPuntoImagen), HttpStatus.CREATED);
    }

    private static RespAlumnoPuntoImagenDTO toDto(RespAlumnoPuntoImagen respAlumnoPuntoImagen) {
        return new RespAlumnoPuntoImagenDTO(
            respAlumnoPuntoImagen.getId(),
            respAlumnoPuntoImagen.getRespuesta(),
            respAlumnoPuntoImagen.getPixelX(),
            respAlumnoPuntoImagen.getPixelY(),
            respAlumnoPuntoImagen.getMarcarImagen().getId(),
            respAlumnoPuntoImagen.getActividadAlumno().getId()
        );
    }

}
