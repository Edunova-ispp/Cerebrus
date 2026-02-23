package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tablero;

@Repository
public interface TableroRepository extends JpaRepository<Tablero, Long> {

}
