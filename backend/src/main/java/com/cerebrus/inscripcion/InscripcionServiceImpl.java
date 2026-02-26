package com.cerebrus.inscripcion;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.inscripcion.InscripcionRepository;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

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
        
        if (usuarioService.findCurrentUser() instanceof Alumno) {
            Alumno alumno = (Alumno) usuarioService.findCurrentUser();
            if (!cursoRepository.existsByCodigo(codigoCurso)) {
                throw new RuntimeException("404 Not Found");
            }else{
                if (inscripcionRepository.findByAlumnoIdAndCursoId(alumno.getId(), cursoRepository.findByCodigo(codigoCurso).getId()) != null) {
                    throw new RuntimeException("400 Bad Request");
                } else {
            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setAlumno(alumno);
            Curso curso=cursoRepository.findByCodigo(codigoCurso);
            inscripcion.setCurso(curso);
            inscripcion.setPuntos(0);
            inscripcion.setFechaInscripcion(LocalDate.now());
            return inscripcionRepository.save(inscripcion);
                }
            }
        } else {
            throw new RuntimeException("401 Unauthorized");
        }
    }
}
