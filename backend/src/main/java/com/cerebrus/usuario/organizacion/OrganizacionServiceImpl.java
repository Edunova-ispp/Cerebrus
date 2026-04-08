package com.cerebrus.usuario.organizacion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.DTO.UsuarioActualizarDTO;


@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {

    private final OrganizacionRepository organizacionRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public OrganizacionServiceImpl(OrganizacionRepository organizacionRepository, UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.organizacionRepository = organizacionRepository;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Maestro> listarMaestros(Long organizacionId, int page, int size) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "listar maestros");
        // Forzar carga de maestros dentro de la transación
        Hibernate.initialize(organizacion.getMaestros());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Maestro> maestros = organizacion.getMaestros().stream()
                .map(this::toSafeMaestro)
                .toList();
        return paginarLista(maestros, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alumno> listarAlumnos(Long organizacionId, int page, int size) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "listar alumnos");
        // Forzar carga de alumnos dentro de la transación
        Hibernate.initialize(organizacion.getAlumnos());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Alumno> alumnos = organizacion.getAlumnos().stream()
                .map(this::toSafeAlumno)
                .toList();
        return paginarLista(alumnos, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long organizacionId, Long usuarioId) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "buscar usuarios");
        // Forzar carga de ambas colecciones dentro de la transacción
        Hibernate.initialize(organizacion.getMaestros());
        Hibernate.initialize(organizacion.getAlumnos());
        // Hacer copias de las colecciones para evitar lazy loading posterior
        List<Usuario> usuarios = Stream.concat(
                        new ArrayList<>(organizacion.getMaestros()).stream().map(m -> (Usuario) m),
                        new ArrayList<>(organizacion.getAlumnos()).stream().map(a -> (Usuario) a))
                .toList();
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        return toSafeUsuario(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long organizacionId, Long usuarioId) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "eliminar usuarios");
        Usuario usuarioAEliminar = buscarUsuario(organizacionId, usuarioId);
        if (usuarioAEliminar instanceof Maestro) {
            organizacion.getMaestros().remove(usuarioAEliminar);
        } else if (usuarioAEliminar instanceof Alumno) {
            organizacion.getAlumnos().remove(usuarioAEliminar);
        }
        usuarioRepository.deleteById(usuarioAEliminar.getId());
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Long organizacionId, Long usuarioId, UsuarioActualizarDTO usuarioActualizado) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "actualizar usuarios");
        Usuario usuarioExistente = buscarUsuario(organizacionId, usuarioId);
        validarNombreUsuarioUnicoEnOrganizacion(organizacion, usuarioExistente.getId(), usuarioActualizado.getNombreUsuario());
        // Solo se permiten actualizar ciertos campos
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setPrimerApellido(usuarioActualizado.getPrimerApellido());
        usuarioExistente.setSegundoApellido(usuarioActualizado.getSegundoApellido());
        usuarioExistente.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        usuarioExistente.setCorreoElectronico(usuarioActualizado.getCorreoElectronico());
        if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().isBlank()) {
            usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
        }
        return toSafeUsuario(usuarioRepository.save(usuarioExistente));
    }

    private Organizacion validarYObtenerOrganizacionPropietaria(Long organizacionId, String accion) {
        Usuario u = usuarioService.findCurrentUser();
        if (!u.getId().equals(organizacionId)) {
            throw new IllegalArgumentException("Solo la organización propietaria puede " + accion);
        }
        Organizacion org = organizacionRepository.findById(organizacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada con ID: " + organizacionId));
        // Forzar carga de suscripciones para evitar lazy loading al serializar
        Hibernate.initialize(org.getSuscripciones());
        return org;
    }

    private <T> Page<T> paginarLista(List<T> elementos, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' no puede ser negativo");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("El parámetro 'size' debe ser mayor que 0");
        }
        Pageable pageable = PageRequest.of(page, size);
        int inicio = (int) pageable.getOffset();
        if (inicio >= elementos.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, elementos.size());
        }
        int fin = Math.min(inicio + pageable.getPageSize(), elementos.size());
        return new PageImpl<>(elementos.subList(inicio, fin), pageable, elementos.size());
    }

    private void validarNombreUsuarioUnicoEnOrganizacion(Organizacion organizacion, Long usuarioIdActual, String nombreUsuarioNuevo) {
        if (nombreUsuarioNuevo == null || nombreUsuarioNuevo.isBlank()) {
            return;
        }
        Hibernate.initialize(organizacion.getMaestros());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Maestro> maestros = organizacion.getMaestros().stream()
                .map(this::toSafeMaestro)
                .toList();
        boolean existeEnMaestros = maestros.stream()
                .filter(m -> !m.getId().equals(usuarioIdActual)).anyMatch(m -> m.getNombreUsuario().equals(nombreUsuarioNuevo));
        Hibernate.initialize(organizacion.getAlumnos());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Alumno> alumnos = organizacion.getAlumnos().stream()
                .map(this::toSafeAlumno)
                .toList();
        boolean existeEnAlumnos = alumnos.stream()
                .filter(a -> !a.getId().equals(usuarioIdActual)).anyMatch(a -> a.getNombreUsuario().equals(nombreUsuarioNuevo));
        if (existeEnMaestros || existeEnAlumnos) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
    }

    private Usuario toSafeUsuario(Usuario usuario) {
        if (usuario instanceof Maestro maestro) {
            return toSafeMaestro(maestro);
        }
        if (usuario instanceof Alumno alumno) {
            return toSafeAlumno(alumno);
        }
        return usuario;
    }

    private Maestro toSafeMaestro(Maestro original) {
        Maestro maestro = new Maestro();
        maestro.setId(original.getId());
        maestro.setNombre(original.getNombre());
        maestro.setPrimerApellido(original.getPrimerApellido());
        maestro.setSegundoApellido(original.getSegundoApellido());
        maestro.setNombreUsuario(original.getNombreUsuario());
        maestro.setCorreoElectronico(original.getCorreoElectronico());
        maestro.setContrasena(original.getContrasena());
        return maestro;
    }

    private Alumno toSafeAlumno(Alumno original) {
        Alumno alumno = new Alumno();
        alumno.setId(original.getId());
        alumno.setNombre(original.getNombre());
        alumno.setPrimerApellido(original.getPrimerApellido());
        alumno.setSegundoApellido(original.getSegundoApellido());
        alumno.setNombreUsuario(original.getNombreUsuario());
        alumno.setCorreoElectronico(original.getCorreoElectronico());
        alumno.setContrasena(original.getContrasena());
        alumno.setPuntos(original.getPuntos());
        return alumno;
    }
}