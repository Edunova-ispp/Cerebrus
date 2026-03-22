package com.cerebrus.tema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.maestro.MaestroRepository;
import com.cerebrus.actividad.ActividadRepository;;

@Service
@Transactional
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;
    private final CursoServiceImpl cursoService;
    private final CursoRepository cursoRepository;
    private final MaestroRepository maestroRepository;
    private final UsuarioService usuarioService;
    private final ActividadRepository actividadRepository;

    @Autowired
    public TemaServiceImpl(TemaRepository temaRepository, CursoRepository cursoRepository, CursoServiceImpl cursoService, MaestroRepository maestroRepository, UsuarioService usuarioService, ActividadRepository actividadRepository) {
        this.temaRepository = temaRepository;
        this.cursoRepository = cursoRepository;
        this.cursoService = cursoService;
        this.maestroRepository = maestroRepository;
        this.usuarioService = usuarioService;
        this.actividadRepository = actividadRepository;
    }

    @Override
    public Tema crearTema(String titulo, Long cursoId, Long maestroId) {
        String tituloNormalizado = normalizeAndValidateTitulo(titulo);

        // Verificar que el maestro existe
        Maestro maestro = maestroRepository.findById(maestroId)
            .orElseThrow(() -> new ResourceNotFoundException("Maestro", "id", maestroId));

        // Verificar que el curso existe y pertenece al maestro
        Curso curso = cursoRepository.findById(cursoId)
            .orElseThrow(() -> new ResourceNotFoundException("Curso", "id", cursoId));

        if (!curso.getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del curso");
        }

        Tema tema = new Tema(tituloNormalizado, curso);
        return temaRepository.save(tema);
    }

    @Override
    public Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId) {
        String tituloNormalizado = normalizeAndValidateTitulo(nuevoTitulo);
        // Verificar que el tema existe y pertenece a un curso del maestro
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema", "id", temaId));

        if (!tema.getCurso().getMaestro().getId().equals(maestroId)) {
            throw new IllegalArgumentException("El maestro no es propietario del tema");
        }

        tema.setTitulo(tituloNormalizado);
        return temaRepository.save(tema);
    }

    @Override
    public List<Tema> ObtenerTemasPorCursoAlumno(Long cursoId) {
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

    @Override
    public List<Tema> ObtenerTemasPorCursoMaestro(Long cursoId) {
        Usuario usuario = usuarioService.findCurrentUser(); 
        if(!(usuario instanceof Maestro)) {
            throw new AccessDeniedException("El usuario no es un maestro.");
        } else {

            List<Tema> temas = temaRepository.findByCursoId(cursoId);

            if (!cursoService.getCursoById(cursoId).getMaestro().getId().equals(usuario.getId())) {
                throw new AccessDeniedException("El maestro no es propietario del curso.");
            }
            return temas;
        }
        
    }

    @Override
    public Tema obtenerTemaPorId(Long temaId) {

        return temaRepository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema", "id", temaId));
    }

    @Override
    public void eliminarTema(Long temaId) {
        
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema", "id", temaId));

        Usuario usuario = usuarioService.findCurrentUser(); 
        if(usuario instanceof Maestro && tema.getCurso().getMaestro().getId().equals(usuario.getId())){
            List<Actividad> actividades = actividadRepository.findByTemaId(temaId);
            actividades.forEach(actividad -> actividadRepository.delete(actividad));
            temaRepository.delete(tema);
        } else {
            throw new IllegalArgumentException("El usuario no tiene permiso para eliminar este tema.");
        }
    }

    private String normalizeAndValidateTitulo(String titulo) {
        if (titulo == null) {
            throw new IllegalArgumentException("El titulo no puede ser nulo");
        }
        String tituloNormalizado = titulo.trim();
        if (tituloNormalizado.isEmpty()) {
            throw new IllegalArgumentException("El titulo no puede estar vacio");
        }
        return tituloNormalizado;
    }

}