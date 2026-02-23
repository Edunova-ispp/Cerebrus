package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

}
