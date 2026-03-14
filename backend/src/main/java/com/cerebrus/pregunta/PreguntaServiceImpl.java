package com.cerebrus.pregunta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.comun.utils.CerebrusUtils;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.actividad.Actividad;

@Service
@Transactional
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final ActividadRepository actividadRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public PreguntaServiceImpl(PreguntaRepository preguntaRepository, 
        ActividadRepository actividadRepository, UsuarioService usuarioService) {
        this.preguntaRepository = preguntaRepository;
        this.actividadRepository = actividadRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public Pregunta crearPregunta(String pregunta, String imagen, Long actId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear preguntas");
        }
        
        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new ResourceNotFoundException("La actividad de la pregunta no existe"));
        
        Pregunta preguntaObj = new Pregunta();
        preguntaObj.setPregunta(pregunta);
        preguntaObj.setImagen(imagen);
        preguntaObj.setActividad(actividad);
        return preguntaRepository.save(preguntaObj);
    }

    @Override
    public Pregunta readPregunta(Long id) {
        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La pregunta no existe"));
        List<RespuestaMaestro> respuestas = preguntaObj.getRespuestas();
        List<RespuestaMaestro> respuestasDesordenadas = CerebrusUtils.shuffleCollection(respuestas).stream().toList();
        preguntaObj.setRespuestas(respuestasDesordenadas);
        return preguntaObj;
    }

    @Override
    @Transactional
    public Pregunta updatePregunta(Long id, String pregunta, String imagen) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar preguntas");
        }

        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La pregunta no existe"));
        preguntaObj.setPregunta(pregunta);
        preguntaObj.setImagen(imagen);
        return preguntaRepository.save(preguntaObj);
    }

    @Override
    @Transactional
    public void deletePregunta(Long id) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar preguntas");
        }
        
        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La pregunta no existe"));
        preguntaRepository.delete(preguntaObj);
    }
}
