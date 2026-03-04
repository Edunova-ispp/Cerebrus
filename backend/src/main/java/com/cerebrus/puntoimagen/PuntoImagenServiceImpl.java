package com.cerebrus.puntoimagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class PuntoImagenServiceImpl implements PuntoImagenService {

    private final PuntoImagenRepository puntoImagenRepository;

    @Autowired
    public PuntoImagenServiceImpl(PuntoImagenRepository puntoImagenRepository) {
        this.puntoImagenRepository = puntoImagenRepository;
    }
}
