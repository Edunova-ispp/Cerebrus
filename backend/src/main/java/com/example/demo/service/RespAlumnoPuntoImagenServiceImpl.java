package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RespAlumnoPuntoImagenRepository;

@Service
@Transactional
public class RespAlumnoPuntoImagenServiceImpl implements RespAlumnoPuntoImagenService {

    private final RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository;

    @Autowired
    public RespAlumnoPuntoImagenServiceImpl(RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository) {
        this.respAlumnoPuntoImagenRepository = respAlumnoPuntoImagenRepository;
    }
}
