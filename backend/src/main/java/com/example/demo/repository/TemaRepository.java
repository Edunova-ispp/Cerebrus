package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tema;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

}
