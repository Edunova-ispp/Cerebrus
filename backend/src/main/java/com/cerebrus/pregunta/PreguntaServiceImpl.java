package com.cerebrus.pregunta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;

    @Autowired
    public PreguntaServiceImpl(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }
}
