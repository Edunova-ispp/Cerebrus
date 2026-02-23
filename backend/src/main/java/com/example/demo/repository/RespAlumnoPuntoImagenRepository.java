package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.RespAlumnoPuntoImagen;

@Repository
public interface RespAlumnoPuntoImagenRepository extends JpaRepository<RespAlumnoPuntoImagen, Long> {

}
