package com.cerebrus.usuario.organizacion;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.dto.CreateUserRequest;

@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {


    private final OrganizacionRepository organizacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    @Autowired
    public OrganizacionServiceImpl(OrganizacionRepository organizacionRepository,
                                   UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder,
                                   UsuarioService usuarioService) {
        this.organizacionRepository = organizacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    @Override
    public void crearUsuario(CreateUserRequest request) {
        Usuario usuarioActual = usuarioService.findCurrentUser();
        
        if (!(usuarioActual instanceof Organizacion)) {
            throw new AccessDeniedException("Solo usuarios con rol ORGANIZACIÓN pueden crear nuevos usuarios.");
        }
        
        Organizacion organizacion = (Organizacion) usuarioActual;
                 

        String rolUpper = request.getRol().toUpperCase();
        if ("ORGANIZACION".equals(rolUpper)) {
            throw new IllegalArgumentException("No se puede crear usuarios con rol ORGANIZACIÓN.");
        }

        
        if (usuarioRepository.existsByNombreUsuario(request.getUsername())) {
            throw new IllegalArgumentException("El username ' " + request.getUsername() + "' ya está registrado.");
        }


        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (usuarioRepository.existsByCorreoElectronico(request.getEmail())) {
                throw new IllegalArgumentException("El email ' " + request.getEmail() + "' ya está registrado.");
            }
        }

        
        Usuario nuevoUsuario = null;
        
        if ("MAESTRO".equals(rolUpper)) {
            nuevoUsuario = new Maestro(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                organizacion
            );
            
        } else if ("ALUMNO".equals(rolUpper)) {
            nuevoUsuario = new Alumno(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                0,
                organizacion
            );
            
        } else {
            throw new IllegalArgumentException("Rol inválido. Use: MAESTRO o ALUMNO.");
        }

        usuarioRepository.save(nuevoUsuario);
        
    }

}
