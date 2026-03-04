package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.tema.Tema;
import org.springframework.security.access.AccessDeniedException;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
@Transactional
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final TemaRepository temaRepository;
    private final UsuarioService usuarioService; // Añadir

    @Autowired
    public ActividadServiceImpl(ActividadRepository actividadRepository, 
        TemaRepository temaRepository, UsuarioService usuarioService) {
        this.actividadRepository = actividadRepository;
        this.temaRepository = temaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public Actividad crearActividadTeoria(String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de teoría");
        }

        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado"));

        Integer maxPosicion = actividadRepository.findMaxPosicionByTemaId(temaId);
        Integer nuevaPosicion = (maxPosicion != null) ? maxPosicion + 1 : 1;

        Actividad actividad = new General(titulo, descripcion, puntuacion, imagen, 
            false, nuevaPosicion, 1, tema, TipoActGeneral.TEORIA);

        return actividadRepository.save(actividad);
    }


    @Override
    public List<Actividad> ObtenerActividadesPorTema(Long temaId) {
        //Esta funcion devuelve una lista con todas las actividades de un tema, 
        return actividadRepository.findByTemaId(temaId);
    }

    @Override
    public void deleteActividad(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
        actividadRepository.delete(actividad);
    }

    @Override
public Actividad updateActividadTeoria(Long id, String titulo, String descripcion) {
    Usuario u = usuarioService.findCurrentUser();
    if (!(u instanceof Maestro)) {
        throw new AccessDeniedException("Solo un maestro puede editar actividades de teoría");
    }

    Actividad actividad = actividadRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

    actividad.setTitulo(titulo);
    actividad.setDescripcion(descripcion);
    return actividadRepository.save(actividad);
}

@Override
public Actividad encontrarActividadPorId(Long id) {
    return actividadRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
}
}
