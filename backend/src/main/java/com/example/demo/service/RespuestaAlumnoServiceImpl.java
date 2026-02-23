package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RespuestaAlumnoRepository;

@Service
@Transactional
public class RespuestaAlumnoServiceImpl implements RespuestaAlumnoService {

    private final RespuestaAlumnoRepository respuestaAlumnoRepository;

    @Autowired
    public RespuestaAlumnoServiceImpl(RespuestaAlumnoRepository respuestaAlumnoRepository) {
        this.respuestaAlumnoRepository = respuestaAlumnoRepository;
    }
}
