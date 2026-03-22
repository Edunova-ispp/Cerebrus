package com.cerebrus.inscripcion;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;

@Service
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioService usuarioService;
    private final CursoRepository cursoRepository;

    @Autowired
    public InscripcionServiceImpl(InscripcionRepository inscripcionRepository, UsuarioService usuarioService, CursoRepository cursoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioService = usuarioService;
        this.cursoRepository = cursoRepository;
    }


    @Override
    public Inscripcion CrearInscripcion(String codigoCurso) {
        String codigoNormalizado = normalizeCodigoCurso(codigoCurso);

        if (!(usuarioService.findCurrentUser() instanceof Alumno alumno)) {
            throw new AccessDeniedException("Solo un alumno puede inscribirse a un curso");
        }

        Curso curso = cursoRepository.findByCodigo(codigoNormalizado);
        if (curso == null) {
            throw new ResourceNotFoundException("Curso", "codigo", codigoNormalizado);
        }

        if (!Boolean.TRUE.equals(curso.getVisibilidad())) {
            throw new AccessDeniedException("El curso no esta disponible para inscripcion");
        }

        Inscripcion existente = inscripcionRepository.findByAlumnoIdAndCursoId(alumno.getId(), curso.getId());
        if (existente != null) {
            throw new IllegalArgumentException("El alumno ya esta inscrito en este curso");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        inscripcion.setPuntos(0);
        inscripcion.setFechaInscripcion(LocalDate.now());
        return inscripcionRepository.save(inscripcion);
    }

    private String normalizeCodigoCurso(String codigoCurso) {
        if (codigoCurso == null) {
            throw new IllegalArgumentException("El codigo del curso es obligatorio");
        }
        String codigoNormalizado = codigoCurso.trim().toUpperCase();

        if (!codigoNormalizado.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("El codigo del curso debe ser alfanumerico");
        }
        return codigoNormalizado;
    }
}