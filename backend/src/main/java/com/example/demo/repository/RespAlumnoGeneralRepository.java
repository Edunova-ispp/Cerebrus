package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.RespAlumnoGeneral;

@Repository
public interface RespAlumnoGeneralRepository extends JpaRepository<RespAlumnoGeneral, Long> {

}
