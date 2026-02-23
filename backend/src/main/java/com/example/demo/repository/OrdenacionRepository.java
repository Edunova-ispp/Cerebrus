package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Ordenacion;

@Repository
public interface OrdenacionRepository extends JpaRepository<Ordenacion, Long> {

}
