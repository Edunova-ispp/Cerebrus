package com.cerebrus.usuario.organizacion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@RestController
@RequestMapping("/api/organizaciones")
@CrossOrigin(origins = "*")
public class OrganizacionController {

    private final OrganizacionService organizacionService;

    @Autowired
    public OrganizacionController(OrganizacionService organizacionService) {
        this.organizacionService = organizacionService;
    }

    @RequestMapping("/maestros")
    public List<Maestro> listarMaestros(Long organizacionId) {
        return organizacionService.listarMaestros(organizacionId);
    }

    @RequestMapping("/alumnos")
    public List<Alumno> listarAlumnos(Long organizacionId) {
        return organizacionService.listarAlumnos(organizacionId);
    }

    
}
