package com.cerebrus.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaestroServiceImpl implements MaestroService {

    private final MaestroRepository maestroRepository;

    @Autowired
    public MaestroServiceImpl(MaestroRepository maestroRepository) {
        this.maestroRepository = maestroRepository;
    }
}
