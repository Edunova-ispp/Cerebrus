package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MarcarImagenService;

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
