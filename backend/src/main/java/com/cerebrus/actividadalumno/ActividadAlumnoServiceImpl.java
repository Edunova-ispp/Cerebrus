package com.cerebrus.actividadalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ActividadAlumnoServiceImpl implements ActividadAlumnoService {

    private final ActividadAlumnoRepository actividadAlumnoRepository;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
    }
}
