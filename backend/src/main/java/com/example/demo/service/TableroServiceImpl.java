package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.TableroRepository;

@Service
@Transactional
public class TableroServiceImpl implements TableroService {

    private final TableroRepository tableroRepository;

    @Autowired
    public TableroServiceImpl(TableroRepository tableroRepository) {
        this.tableroRepository = tableroRepository;
    }
}
