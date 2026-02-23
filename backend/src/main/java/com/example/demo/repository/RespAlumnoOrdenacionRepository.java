package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.RespAlumnoOrdenacion;

@Repository
public interface RespAlumnoOrdenacionRepository extends JpaRepository<RespAlumnoOrdenacion, Long> {

}
