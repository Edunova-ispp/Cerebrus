package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MarcarImagen;

@Repository
public interface MarcarImagenRepository extends JpaRepository<MarcarImagen, Long> {

}
