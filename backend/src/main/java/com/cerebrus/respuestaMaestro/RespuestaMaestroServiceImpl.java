package com.cerebrus.respuestaMaestro;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.actividad.general.General;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;

@Service
@Transactional
public class RespuestaMaestroServiceImpl implements RespuestaMaestroService {

    private final RespuestaMaestroRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public RespuestaMaestroServiceImpl(RespuestaMaestroRepository respuestaRepository, 
        PreguntaRepository preguntaRepository, UsuarioService usuarioService) {
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public RespuestaMaestro crearRespuesta(String respuesta, String imagen, Boolean correcta, Long preguntaId) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear respuestas");
        }

        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new ResourceNotFoundException("La pregunta de la respuesta no existe"));
        
    
        if (pregunta.getActividad() != null && pregunta.getActividad() instanceof General) {
            General general = (General) pregunta.getActividad();
            
            if (TipoActGeneral.ABIERTA.equals(general.getTipo())) {
                List<RespuestaMaestro> respuestasExistentes = respuestaRepository.findRespuestaByPreguntaId(preguntaId);
                if (respuestasExistentes != null && !respuestasExistentes.isEmpty()) {
                    throw new IllegalArgumentException("Esta pregunta ya tiene una respuesta. En actividades de tipo ABIERTA solo se permite una respuesta por pregunta");
                }
            }
        }
        
        if (!pregunta.getActividad().getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede crear respuestas para sus preguntas");
        }
        RespuestaMaestro respuestaObj = new RespuestaMaestro();
        respuestaObj.setRespuesta(respuesta);
        respuestaObj.setImagen(imagen);
        respuestaObj.setCorrecta(correcta);
        respuestaObj.setPregunta(pregunta);
        return respuestaRepository.save(respuestaObj);
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaMaestro readRespuesta(Long id) {
        return respuestaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La respuesta no existe"));
    }

    @Override
    @Transactional
    public RespuestaMaestro updateRespuesta(Long id, String respuesta, String imagen, Boolean correcta) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede actualizar respuestas");
        }

        RespuestaMaestro respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La respuesta no existe"));
        if (!respuestaObj.getPregunta().getActividad().getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede actualizar respuestas para sus preguntas");
        }
        respuestaObj.setRespuesta(respuesta);
        respuestaObj.setImagen(imagen);
        respuestaObj.setCorrecta(correcta);
        return respuestaRepository.save(respuestaObj);
    }

    @Override
    @Transactional
    public void deleteRespuesta(Long id) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar respuestas");
        }

        RespuestaMaestro respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La respuesta no existe"));
        if (!respuestaObj.getPregunta().getActividad().getTema().getCurso().getMaestro().getId().equals(u.getId())) {
            throw new AccessDeniedException("Solo el maestro del curso puede eliminar respuestas para sus preguntas");
        }
        respuestaRepository.delete(respuestaObj);
    }

    @Override
    public List<RespuestaMaestro> encontrarRespuestasPorPreguntaId(Long preguntaId) {
        return respuestaRepository.findRespuestaByPreguntaId(preguntaId);
    }
}
