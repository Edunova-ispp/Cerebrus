package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.tema.TemaRepository;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;
    private final CursoServiceImpl cursoService;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository, CursoServiceImpl cursoService) {
        this.temaRepository = temaRepository;
        this.cursoService = cursoService;
    }

    public List<Tema> ObtenerTemasPorCursoAlumno(Integer cursoId) {
        //Esta funcion devuelve una lista con todos los temas de un curso, 
        // si el usuario está inscrito en el curso, si no lo está devuelve una excepcion 403 Forbidden.
        List<Curso> cursos = cursoService.ObtenerCursosUsuarioLogueado();
        boolean estaInscrito = cursos.stream().anyMatch(c -> c.getId().equals(cursoId));
        if(!estaInscrito){
            throw new AccessDeniedException("El alumno logueado no está inscrito en este curso.");
        } else {
            return temaRepository.findByCursoId(cursoId);
        }
    }
}
