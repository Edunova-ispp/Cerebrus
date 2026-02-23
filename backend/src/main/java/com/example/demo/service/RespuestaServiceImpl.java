package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RespuestaRepository;

@Service
@Transactional
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaRepository respuestaRepository;

    @Autowired
    public RespuestaServiceImpl(RespuestaRepository respuestaRepository) {
        this.respuestaRepository = respuestaRepository;
    }
}
