package com.cerebrus.usuario.alumno;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

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

}
