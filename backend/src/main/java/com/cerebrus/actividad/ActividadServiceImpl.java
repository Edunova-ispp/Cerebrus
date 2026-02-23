package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;

    @Autowired
    public ActividadServiceImpl(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }
}
