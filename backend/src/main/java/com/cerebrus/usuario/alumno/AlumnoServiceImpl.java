package com.cerebrus.usuario.alumno;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.dto.AlumnoDTO;
import com.cerebrus.usuario.alumno.dto.AlumnosPageDTO;
import com.cerebrus.usuario.maestro.Maestro;

@Service
@Transactional
public class AlumnoServiceImpl implements AlumnoService {

    private static final Logger log = LoggerFactory.getLogger(AlumnoServiceImpl.class);

    private final AlumnoRepository alumnoRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public AlumnoServiceImpl(AlumnoRepository alumnoRepository, 
                            ActividadAlumnoRepository actividadAlumnoRepository,
                            UsuarioService usuarioService) {
        this.alumnoRepository = alumnoRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerTotalPuntosAlumno() {
        Usuario usuario = usuarioService.findCurrentUser();
        
        if (!(usuario instanceof Alumno)) {
            throw new AccessDeniedException("El usuario actual no es un alumno");
        }
        
        Alumno alumno = (Alumno) usuario;
        
        List<ActividadAlumno> actividadesAlumno = actividadAlumnoRepository.findByAlumnoId(alumno.getId());
        
        Integer totalPuntos = 0;
        
        for (ActividadAlumno actividadAlumno : actividadesAlumno) {
            if (actividadAlumno.getEstadoActividad() == EstadoActividad.TERMINADA) {
                if (actividadAlumno.getPuntuacion() != null) {
                    totalPuntos += actividadAlumno.getPuntuacion();
                }
            }
        }
        
        return totalPuntos;
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnosPageDTO obtenerAlumnosDeOrganizacion(int numeroPagina, int tamanioPagina, String busqueda) {
        Usuario usuario = usuarioService.findCurrentUser();
        
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("El usuario actual no es un maestro");
        }
        
        Maestro maestro = (Maestro) usuario;
        
        if (maestro.getOrganizacion() == null) {
            throw new IllegalArgumentException("El maestro no tiene una organización asignada");
        }
        
        Long organizacionId = maestro.getOrganizacion().getId();
        
        Pageable pageable = PageRequest.of(numeroPagina, tamanioPagina);
        
        Page<Alumno> alumnoPage;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            alumnoPage = alumnoRepository.buscarAlumnosPorOrganizacion(organizacionId, busqueda, pageable);
        } else {
            alumnoPage = alumnoRepository.findByOrganizacionId(organizacionId, pageable);
        }
        
        List<AlumnoDTO> alumnosDTOs = alumnoPage.getContent().stream()
                .map(alumno -> new AlumnoDTO(
                        alumno.getNombre(),
                        alumno.getPrimerApellido(),
                        alumno.getSegundoApellido(),
                        alumno.getCorreoElectronico()
                ))
                .collect(Collectors.toList());
        
        return new AlumnosPageDTO(
                alumnosDTOs,
                (int) alumnoPage.getTotalElements(),
                numeroPagina,
                alumnoPage.getTotalPages(),
                alumnoPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnosPageDTO obtenerAlumnosNoInscritosEnCurso(int numeroPagina, int tamanioPagina, String busqueda, Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser();
        
        if (!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("El usuario actual no es un maestro");
        }
        
        Maestro maestro = (Maestro) usuario;
        
        if (maestro.getOrganizacion() == null) {
            throw new IllegalArgumentException("El maestro no tiene una organización asignada");
        }
        
        Long organizacionId = maestro.getOrganizacion().getId();
        
        Pageable pageable = PageRequest.of(numeroPagina, tamanioPagina);
        
        Page<Alumno> alumnoPage;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            alumnoPage = alumnoRepository.buscarAlumnosNoInscritosEnCurso(organizacionId, cursoId, busqueda, pageable);
        } else {
            alumnoPage = alumnoRepository.findAlumnosNoInscritosEnCurso(organizacionId, cursoId, pageable);
        }
        
        List<AlumnoDTO> alumnosDTOs = alumnoPage.getContent().stream()
                .map(alumno -> new AlumnoDTO(
                        alumno.getId(),
                        alumno.getNombre(),
                        alumno.getPrimerApellido(),
                        alumno.getSegundoApellido(),
                        alumno.getCorreoElectronico()
                ))
                .collect(Collectors.toList());
        
        return new AlumnosPageDTO(
                alumnosDTOs,
                (int) alumnoPage.getTotalElements(),
                numeroPagina,
                alumnoPage.getTotalPages(),
                alumnoPage.isLast()
        );
    }

}
