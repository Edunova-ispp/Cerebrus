package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.MarcarImagenRepository;

@Service
@Transactional
public class MarcarImagenServiceImpl implements MarcarImagenService {

    private final MarcarImagenRepository marcarImagenRepository;

    @Autowired
    public MarcarImagenServiceImpl(MarcarImagenRepository marcarImagenRepository) {
        this.marcarImagenRepository = marcarImagenRepository;
    }
}
