package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.TableroService;

@RestController
@RequestMapping("/api/tableros")
@CrossOrigin(origins = "*")
public class TableroController {

    private final TableroService tableroService;

    @Autowired
    public TableroController(TableroService tableroService) {
        this.tableroService = tableroService;
    }
}
