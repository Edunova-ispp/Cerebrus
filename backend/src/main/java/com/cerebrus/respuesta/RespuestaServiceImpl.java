package com.cerebrus.respuesta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.pregunta.Pregunta;

@Service
@Transactional
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public RespuestaServiceImpl(RespuestaRepository respuestaRepository, 
        PreguntaRepository preguntaRepository, UsuarioService usuarioService) {
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public Respuesta crearRespuesta(String respuesta, String imagen, Boolean correcta, Long preguntaId) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new RuntimeException("Solo un maestro puede crear respuestas");
        }

        Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta de la respuesta no existe"));
        
        Respuesta respuestaObj = new Respuesta();
        respuestaObj.setRespuesta(respuesta);
        respuestaObj.setImagen(imagen);
        respuestaObj.setCorrecta(correcta);
        respuestaObj.setPregunta(pregunta);
        return respuestaRepository.save(respuestaObj);
    }

    @Override
    @Transactional(readOnly = true)
    public Respuesta readRespuesta(Long id) {
        return respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
    }

    @Override
    @Transactional
    public Respuesta updateRespuesta(Long id, String respuesta, String imagen, Boolean correcta) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new RuntimeException("Solo un maestro puede actualizar respuestas");
        }

        Respuesta respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
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
            throw new RuntimeException("Solo un maestro puede eliminar respuestas");
        }

        Respuesta respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
        respuestaRepository.delete(respuestaObj);
    }
}
