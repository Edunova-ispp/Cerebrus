package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.exceptions.ResourceNotFoundException;


@Service
@Transactional
public class RespuestaAlumnoServiceImpl implements RespuestaAlumnoService {

    private final RespuestaAlumnoRepository respuestaAlumnoRepository;

    @Autowired
    public RespuestaAlumnoServiceImpl(RespuestaAlumnoRepository respuestaAlumnoRepository) {
        this.respuestaAlumnoRepository = respuestaAlumnoRepository;
    }

    @Override
    public RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id) {
        return respuestaAlumnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumno", "id", id));
    }

    @Override
    public RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id) {
        RespuestaAlumno respuestaAlumno = respuestaAlumnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumno", "id", id));

        Boolean esCorrecta = respuestaAlumno.getCorrecta();
        if (esCorrecta == null) {
            esCorrecta = true; // Si es null, lo consideramos como no marcada, así que la marcamos como correcta
        } else {
            esCorrecta = !esCorrecta;
        }

        respuestaAlumno.setCorrecta(esCorrecta);
        return respuestaAlumnoRepository.save(respuestaAlumno);
    }

    
}
