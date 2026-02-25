package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.MaestroRepository;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;
    private final CursoServiceImpl cursoService;
    private final CursoRepository cursoRepository;
    private final MaestroRepository maestroRepository;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository, CursoRepository cursoRepository, MaestroRepository maestroRepository, CursoServiceImpl cursoService) {
        this.temaRepository = temaRepository;
        this.cursoRepository = cursoRepository;
        this.maestroRepository = maestroRepository;
        this.cursoService = cursoService;
    }

    @Override
    public Tema crearTema(String titulo, Long cursoId, Long maestroId) {
        // Verificar que el curso existe y pertenece al maestro
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Maestro maestro = maestroRepository.findById(maestroId)
                .orElseThrow(() -> new IllegalArgumentException("Maestro no encontrado"));

        if (!curso.getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del curso");
        }

        Tema tema = new Tema(titulo, curso);
        return temaRepository.save(tema);
    }

    @Override
    public Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId) {
        // Verificar que el tema existe y pertenece a un curso del maestro
        Tema tema = temaRepository.findById(temaId)
                .orElseThrow(() -> new IllegalArgumentException("Tema no encontrado"));

        if (!tema.getCurso().getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del tema");
        }

        tema.setTitulo(nuevoTitulo);
        return temaRepository.save(tema);
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
