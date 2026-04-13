package com.cerebrus.usuario;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUser() {
        Usuario user = usuarioService.findCurrentUser();
        return toSafeMap(user);
    }

    @PutMapping("/me")
    @Transactional
    public Map<String, Object> updateCurrentUser(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String nombreUsuario,
            @RequestParam(required = false) String correoElectronico) {
        Usuario user = usuarioService.findCurrentUser();
        if (nombre != null && !nombre.isBlank()) user.setNombre(nombre);
        if (primerApellido != null && !primerApellido.isBlank()) user.setPrimerApellido(primerApellido);
        if (segundoApellido != null) user.setSegundoApellido(segundoApellido);
        if (nombreUsuario != null && !nombreUsuario.isBlank()) user.setNombreUsuario(nombreUsuario);
        if (correoElectronico != null && !correoElectronico.isBlank()) user.setCorreoElectronico(correoElectronico);
        Usuario saved = usuarioRepository.save(user);
        return toSafeMap(saved);
    }

    private Map<String, Object> toSafeMap(Usuario user) {
        Map<String, Object> data = new HashMap<>();
        if (user == null) {
            return data;
        }

        String nombreOrganizacion = null;

        if (user instanceof Organizacion org) {
            nombreOrganizacion = org.getNombreCentro();
        } else if (user instanceof Maestro maestro) {
            if (maestro.getOrganizacion() != null) {
                nombreOrganizacion = maestro.getOrganizacion().getNombreCentro();
            }
        } else if (user instanceof Alumno alumno) {
            if (alumno.getOrganizacion() != null) {
                nombreOrganizacion = alumno.getOrganizacion().getNombreCentro();
            }
        }

        data.put("id", user.getId());
        data.put("nombre", user.getNombre());
        data.put("primerApellido", user.getPrimerApellido());
        data.put("segundoApellido", user.getSegundoApellido());
        data.put("nombreUsuario", user.getNombreUsuario());
        data.put("correoElectronico", user.getCorreoElectronico());
        data.put("nombreOrganizacion", nombreOrganizacion);
        return data;
    }
}
