package com.cerebrus.auth;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// BUSCADOR DE USUARIOS PARA AUTENTICACIÓN
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuarioOrCorreoElectronico(identificador, identificador)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return UserDetailsImpl.build(usuario);
    }
}