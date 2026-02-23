package com.cerebrus.actividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class GeneralServiceImpl implements GeneralService {

    private final GeneralRepository generalRepository;

    @Autowired
    public GeneralServiceImpl(GeneralRepository generalRepository) {
        this.generalRepository = generalRepository;
    }
}
