package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.OrdenacionService;

@RestController
@RequestMapping("/api/ordenaciones")
@CrossOrigin(origins = "*")
public class OrdenacionController {

    private final OrdenacionService ordenacionService;

    @Autowired
    public OrdenacionController(OrdenacionService ordenacionService) {
        this.ordenacionService = ordenacionService;
    }
}
