package com.cerebrus.usuario.organizacion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {

    private final OrganizacionRepository organizacionRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public OrganizacionServiceImpl(OrganizacionRepository organizacionRepository, UsuarioService usuarioService) {
        this.organizacionRepository = organizacionRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Maestro> listarMaestros(Long organizacionId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new IllegalArgumentException("Solo una organización puede listar maestros");
        }
        if(!u.getId().equals(organizacionId)) {
            throw new IllegalArgumentException("Solo la organización propietaria puede listar sus maestros");
        }
        Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada con ID: " + organizacionId));
        return organizacion.getMaestros();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alumno> listarAlumnos(Long organizacionId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new IllegalArgumentException("Solo una organización puede listar alumnos");
        }
        if(!u.getId().equals(organizacionId)) {
            throw new IllegalArgumentException("Solo la organización propietaria puede listar sus alumnos");
        }
        Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada con ID: " + organizacionId));
        return organizacion.getAlumnos();
    }
}
