package com.cerebrus.tema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
public class TemaController {

    private final TemaService temaService;

    @Autowired
    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }
}
