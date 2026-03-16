package com.cerebrus.auth;

import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, 
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByNombreUsuario(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByCorreoElectronico(email);
    }

    public void registrarUsuario(SignupRequest request) {
        Usuario nuevoUsuario = null;

        String tipo = request.getTipoUsuario().toUpperCase();

        switch (tipo) {
            case "ALUMNO":
                Alumno alumno = new Alumno();
                Integer puntosIniciales = (request.getPuntos() != null) ? request.getPuntos() : 0;
                alumno.setPuntos(puntosIniciales);
                nuevoUsuario = alumno;
                break;
            case "MAESTRO":
                nuevoUsuario = new Maestro();
                break;
            case "ORGANIZACION":
                Organizacion org = new Organizacion();
                org.setNombreCentro(request.getNombreCentro()); 
                nuevoUsuario = org;
                break;
            default:
                throw new IllegalArgumentException("Tipo de usuario inválido. Use: ALUMNO, MAESTRO o ORGANIZACION");
        }

        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPrimerApellido(request.getPrimerApellido());
        nuevoUsuario.setSegundoApellido(request.getSegundoApellido());
        nuevoUsuario.setNombreUsuario(request.getUsername());
        nuevoUsuario.setCorreoElectronico(request.getEmail());
        nuevoUsuario.setContrasena(passwordEncoder.encode(request.getPassword()));

        usuarioRepository.save(nuevoUsuario);
    }
}