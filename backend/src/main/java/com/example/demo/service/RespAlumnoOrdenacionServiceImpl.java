package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RespAlumnoOrdenacionRepository;

@Service
@Transactional
public class RespAlumnoOrdenacionServiceImpl implements RespAlumnoOrdenacionService {

    private final RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository;

    @Autowired
    public RespAlumnoOrdenacionServiceImpl(RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository) {
        this.respAlumnoOrdenacionRepository = respAlumnoOrdenacionRepository;
    }
}
