package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ActividadAlumno;

@Repository
public interface ActividadAlumnoRepository extends JpaRepository<ActividadAlumno, Long> {

}
