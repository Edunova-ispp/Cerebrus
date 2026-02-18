package com.example.demo.controller;

import com.example.demo.entity.TestEntity;
import com.example.demo.repository.TestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(
    origins = {
        "https://cerebrus-frontend.onrender.com",
        "http://localhost:5173",
        "http://localhost:3000"
    }
)
public class TestController {

    private final TestRepository repo;

    public TestController(TestRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public TestEntity create(@RequestParam String name) {
        TestEntity e = new TestEntity();
        e.setName(name);
        return repo.save(e);
    }

    @GetMapping
    public List<TestEntity> all() {
        return repo.findAll();
    }
}