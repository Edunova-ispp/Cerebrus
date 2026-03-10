package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.tema.Tema;
import org.springframework.security.access.AccessDeniedException;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
@Transactional
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final TemaService temaService;
    private final UsuarioService usuarioService; 

    @Autowired
    public ActividadServiceImpl(ActividadRepository actividadRepository, 
        TemaService temaService, UsuarioService usuarioService) {
        this.actividadRepository = actividadRepository;
        this.temaService = temaService;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<Actividad> ObtenerActividadesPorTema(Long temaId) {
        return actividadRepository.findByTemaId(temaId);
    }

    @Override
    public Actividad encontrarActividadPorIdMaestro(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta actividad");
        } else {

            Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
            	
            if (!actividad.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
                throw new AccessDeniedException("La actividad que buscas pertenece a un curso que no es tuyo");
            }
            return actividad; 
        }
    }

    @Override
    public Actividad encontrarActividadPorIdAlumno(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta actividad");
        } else {
            Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
        	
            List<Inscripcion> inscripciones = actividad.getTema().getCurso().getInscripciones();
            for (Inscripcion inscripcion : inscripciones) {
                if (inscripcion.getAlumno().getId().equals(u.getId())) {
                    return actividad; 
                }
            }
            throw new AccessDeniedException("La actividad que buscas pertenece a un curso al que no estás inscrito");
        }
    }

    @Override
    public void deleteActividad(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede borrar actividades");
        } else {
            Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
            
            if (!actividad.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
                throw new AccessDeniedException("No puedes eliminar actividades de cursos que no son tuyos");
            }
        actividadRepository.delete(actividad);
        }   
    }

    @Override
    public Actividad crearActividadTeoria(String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de teoría");
        }

        Tema tema = temaService.obtenerTemaPorId(temaId);

        Integer maxPosicion = actividadRepository.findMaxPosicionByTemaId(temaId);
        Integer nuevaPosicion = (maxPosicion != null) ? maxPosicion + 1 : 1;

        Actividad actividad = new General(titulo, descripcion, 0, imagen, 
            false, nuevaPosicion, 1, tema, TipoActGeneral.TEORIA);

        return actividadRepository.save(actividad);
    }

    @Override
    public Actividad updateActividadTeoria(Long id, String titulo, String descripcion) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede editar actividades de teoría");
        } else {
            Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
                
            if (!actividad.getTema().getCurso().getMaestro().getId().equals(u.getId())) {
                throw new AccessDeniedException("No puedes editar actividades de cursos que no son tuyos");
            }
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        return actividadRepository.save(actividad);
        }
    }

    
}
