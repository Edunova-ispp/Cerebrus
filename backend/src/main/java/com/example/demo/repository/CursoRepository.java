package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

}
