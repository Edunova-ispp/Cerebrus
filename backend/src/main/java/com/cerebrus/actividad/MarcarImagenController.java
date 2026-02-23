package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/marcar-imagenes")
@CrossOrigin(origins = "*")
public class MarcarImagenController {

    private final MarcarImagenService marcarImagenService;

    @Autowired
    public MarcarImagenController(MarcarImagenService marcarImagenService) {
        this.marcarImagenService = marcarImagenService;
    }
}
