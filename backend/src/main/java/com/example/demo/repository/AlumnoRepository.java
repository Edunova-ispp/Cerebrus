package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Alumno;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

}
