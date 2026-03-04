package com.cerebrus.respuestaalumno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RespAlumnoPuntoImagenServiceImpl implements RespAlumnoPuntoImagenService {

    private final RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository;

    @Autowired
    public RespAlumnoPuntoImagenServiceImpl(RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository) {
        this.respAlumnoPuntoImagenRepository = respAlumnoPuntoImagenRepository;
    }
}
