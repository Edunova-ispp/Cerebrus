package com.cerebrus.inscripcion;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.usuario.Usuario;
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
    public Inscripcion crearInscripcion(String codigoCurso) {
        Usuario current = usuarioService.findCurrentUser();
        if (current instanceof Alumno) {
            Alumno alumno = (Alumno) current;
            if (!cursoRepository.existsByCodigo(codigoCurso)) {
                throw new RuntimeException("404 Not Found");
            }else{
                Curso curso = cursoRepository.findByCodigo(codigoCurso);
                if (!Boolean.TRUE.equals(curso.getVisibilidad())) {
                    throw new RuntimeException("403 Forbidden");
                }
                if (inscripcionRepository.findByAlumnoIdAndCursoId(alumno.getId(), curso.getId()) != null) {
                    throw new RuntimeException("400 Bad Request");
                } else {
                    Inscripcion inscripcion = new Inscripcion();
                    inscripcion.setAlumno(alumno);
                    inscripcion.setCurso(curso);
                    inscripcion.setPuntos(0);
                    inscripcion.setFechaInscripcion(LocalDate.now());
                    return inscripcionRepository.save(inscripcion);
                }
            }
        } else {
            throw new AccessDeniedException("Solo un alumno puede inscribirse en un curso");
        }
    }
}