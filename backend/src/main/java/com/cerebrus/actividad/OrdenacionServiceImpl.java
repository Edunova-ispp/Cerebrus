package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OrdenacionServiceImpl implements OrdenacionService {

    private final OrdenacionRepository ordenacionRepository;

    @Autowired
    public OrdenacionServiceImpl(OrdenacionRepository ordenacionRepository) {
        this.ordenacionRepository = ordenacionRepository;
    }
}
