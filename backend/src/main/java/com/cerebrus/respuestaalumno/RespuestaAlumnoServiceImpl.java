package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RespuestaAlumnoServiceImpl implements RespuestaAlumnoService {

    private final RespuestaAlumnoRepository respuestaAlumnoRepository;

    @Autowired
    public RespuestaAlumnoServiceImpl(RespuestaAlumnoRepository respuestaAlumnoRepository) {
        this.respuestaAlumnoRepository = respuestaAlumnoRepository;
    }
}
