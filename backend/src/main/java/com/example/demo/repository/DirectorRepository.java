package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Director;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {

}
