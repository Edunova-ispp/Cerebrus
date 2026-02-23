package com.cerebrus.tema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.tema.TemaRepository;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository) {
        this.temaRepository = temaRepository;
    }
}
