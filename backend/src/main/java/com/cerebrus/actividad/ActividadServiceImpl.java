package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.general.General;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.comun.utils.AccesoActividadAlumnoUtils;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

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
    public Actividad crearActTeoria(String titulo, String descripcion, String imagen, Long temaId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de teoría");
        }

        Tema tema = temaService.encontrarTemaPorId(temaId);

        if (!tema.getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("No puedes crear actividades para temas que no son tuyos");
        }

        Integer maxPosicion = actividadRepository.findMaxPosicionByTemaId(temaId);
        Integer nuevaPosicion = (maxPosicion != null) ? maxPosicion + 1 : 1;

        // Las actividades de teoría no aportan puntuación, pero el campo requiere mínimo 1 por validación
        Actividad actividad = new General(titulo, descripcion, Integer.valueOf(1), imagen,
            Boolean.FALSE, "", nuevaPosicion, Integer.valueOf(1), tema, TipoActGeneral.TEORIA,
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        return actividadRepository.save(actividad);
    }

    @Override
    public Actividad encontrarActTeoriaPorId(Long id) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta actividad");
        } else {
            Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

            if (!Boolean.TRUE.equals(actividad.getTema().getCurso().getVisibilidad())) {
                throw new AccessDeniedException("La actividad que buscas pertenece a un curso oculto");
            }
        	
            List<Inscripcion> inscripciones = actividad.getTema().getCurso().getInscripciones();
            for (Inscripcion inscripcion : inscripciones) {
                if (inscripcion.getAlumno().getId().equals(current.getId())) {
                    AccesoActividadAlumnoUtils.validarActividadDesbloqueadaParaAlumno(actividad, current.getId());
                    return actividad; 
                }
            }
            throw new AccessDeniedException("La actividad que buscas pertenece a un curso al que no estás inscrito");
        }
    }

    @Override
    public Actividad encontrarActTeoriaMaestroPorId(Long id) {
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
    public List<Actividad> encontrarActividadesPorTema(Long temaId) {
        return actividadRepository.findByTemaId(temaId);
    }

    @Override
    public Actividad actualizarActTeoria(Long id, String titulo, String descripcion,String imagen ) {
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
            actividad.setImagen(imagen);
            return actividadRepository.save(actividad);
        }
    }

    @Override
    public void eliminarActTeoriaPorId(Long id) {
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
    
}
