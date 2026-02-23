package com.cerebrus.respuesta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaRepository respuestaRepository;

    @Autowired
    public RespuestaServiceImpl(RespuestaRepository respuestaRepository) {
        this.respuestaRepository = respuestaRepository;
    }
}
