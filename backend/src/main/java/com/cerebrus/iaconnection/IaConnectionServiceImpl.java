package com.cerebrus.iaconnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IaConnectionServiceImpl implements IaConnectionService {


    @Autowired
    public IaConnectionServiceImpl() {
    }
}
