package com.cerebrus.usuario.organizacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    @Autowired
    public OrganizacionServiceImpl(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

}
