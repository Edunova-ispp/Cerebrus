package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.TemaRepository;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository) {
        this.temaRepository = temaRepository;
    }
}
