package com.cerebrus.puntoimagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
