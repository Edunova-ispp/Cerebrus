package com.cerebrus.usuario;

import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.organizacion.Organizacion;

@Service
public class UsuarioService {

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Usuario findByUsername(String username, String email) {
        return userRepository.findByNombreUsuarioOrCorreoElectronico(username, email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username or email", username + " / " + email));
    }

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional(readOnly = true)
    public Usuario findCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            throw new ResourceNotFoundException("Nobody authenticated!");
        else
            return userRepository.findByNombreUsuarioOrCorreoElectronico(auth.getName(), auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "NombreUsuario", auth.getName()));
    }

    public Boolean existsUser(String username) {
        return userRepository.existsByNombreUsuario(username);
    }

    @Transactional(readOnly = true)
    public Iterable<Usuario> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public Usuario saveUser(@Valid Usuario user) throws DataAccessException {
        sanitizeAndValidateUser(user);
        if (user.getContrasena() != null && !user.getContrasena().trim().isEmpty()) {
            user.setContrasena(passwordEncoder.encode(user.getContrasena()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public Usuario updateUser(@Valid Usuario user, Long idToUpdate) {
        sanitizeAndValidateUser(user);
        Usuario toUpdate = findById(idToUpdate);
        if (user.getContrasena() != null && !user.getContrasena().trim().isEmpty()) {
            user.setContrasena(passwordEncoder.encode(user.getContrasena()));
        } else {
            BeanUtils.copyProperties(user, toUpdate, "id", "contrasena");
            userRepository.save(toUpdate);
            return toUpdate;
        }
        BeanUtils.copyProperties(user, toUpdate, "id");
        userRepository.save(toUpdate);

        return toUpdate;
    }

    @Transactional
    public void deleteUser(Long id) {
        Usuario toDelete = findById(id);
        this.userRepository.delete(toDelete);
    }

    private void sanitizeAndValidateUser(Usuario user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (user.getNombre() != null) {
            user.setNombre(user.getNombre().trim());
        }
        if (user.getPrimerApellido() != null) {
            user.setPrimerApellido(user.getPrimerApellido().trim());
        }
        if (user.getSegundoApellido() != null) {
            String segundoApellido = user.getSegundoApellido().trim();
            user.setSegundoApellido(segundoApellido.isEmpty() ? null : segundoApellido);
        }
        if (user.getNombreUsuario() != null) {
            user.setNombreUsuario(user.getNombreUsuario().trim());
        }
        if (user.getCorreoElectronico() != null) {
            user.setCorreoElectronico(user.getCorreoElectronico().trim().toLowerCase());
        }
        if (user.getContrasena() != null) {
            user.setContrasena(user.getContrasena().trim());
        }

        if (user instanceof Alumno alumno && alumno.getPuntos() != null && alumno.getPuntos() < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }

        if (user instanceof Organizacion organizacion) {
            String nombreCentro = organizacion.getNombreCentro();
            if (nombreCentro == null || nombreCentro.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del centro no puede estar vacío");
            }
            organizacion.setNombreCentro(nombreCentro.trim());
        }
    }
}