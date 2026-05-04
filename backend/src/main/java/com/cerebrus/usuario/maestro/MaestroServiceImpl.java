package com.cerebrus.usuario.maestro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
@Transactional
public class MaestroServiceImpl implements MaestroService {

    private final MaestroRepository maestroRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public MaestroServiceImpl(MaestroRepository maestroRepository, UsuarioService usuarioService) {
        this.maestroRepository = maestroRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public Maestro obtenerMaestroActual() {
        Usuario usuario = usuarioService.findCurrentUser();
        
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("El usuario actual no es un maestro");
        }
        
        Maestro maestro = (Maestro) usuario;
        
        if (maestro.getOrganizacion() == null) {
            throw new IllegalArgumentException("El maestro no tiene una organización asignada");
        }
        
        return maestro;
    }
}

