package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RespAlumnoGeneralRepository;

@Service
@Transactional
public class RespAlumnoGeneralServiceImpl implements RespAlumnoGeneralService {

    private final RespAlumnoGeneralRepository respAlumnoGeneralRepository;

    @Autowired
    public RespAlumnoGeneralServiceImpl(RespAlumnoGeneralRepository respAlumnoGeneralRepository) {
        this.respAlumnoGeneralRepository = respAlumnoGeneralRepository;
    }
}
