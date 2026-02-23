package com.cerebrus.pregunta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/preguntas")
@CrossOrigin(origins = "*")
public class PreguntaController {

    private final PreguntaService preguntaService;

    @Autowired
    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }
}
