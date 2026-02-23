package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.General;

@Repository
public interface GeneralRepository extends JpaRepository<General, Long> {

}
