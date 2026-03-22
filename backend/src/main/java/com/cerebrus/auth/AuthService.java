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
        // Validación de datos sanitaria: eliminar espacios en blanco
        String nombre = request.getNombre().trim();
        String primerApellido = request.getPrimerApellido().trim();
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        
        // Validaciones de negocio
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (primerApellido.isEmpty()) {
            throw new IllegalArgumentException("El primer apellido es obligatorio");
        }
        if (username.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        
        // Validación de puntos iniciales
        Integer puntos = request.getPuntos();
        if (puntos != null && puntos < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
        
        Usuario nuevoUsuario = null;
        String tipo = request.getTipoUsuario().toUpperCase().trim();

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
                String nombreCentro = request.getNombreCentro();
                if (nombreCentro != null && !nombreCentro.trim().isEmpty()) {
                    org.setNombreCentro(nombreCentro.trim());
                } else {
                    throw new IllegalArgumentException("El nombre del centro es obligatorio para tipo ORGANIZACION");
                }
                nuevoUsuario = org;
                break;
            default:
                throw new IllegalArgumentException("Tipo de usuario inválido. Use: ALUMNO, MAESTRO o ORGANIZACION");
        }

        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPrimerApellido(request.getPrimerApellido());
        if(request.getSegundoApellido() != null) {
            nuevoUsuario.setSegundoApellido(request.getSegundoApellido().trim());
        }
        nuevoUsuario.setNombreUsuario(request.getUsername());
        nuevoUsuario.setCorreoElectronico(request.getEmail());
        nuevoUsuario.setContrasena(passwordEncoder.encode(request.getPassword()));

        usuarioRepository.save(nuevoUsuario);
    }
}