package com.cerebrus.respuesta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.pregunta.Pregunta;

@Service
@Transactional
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;

    @Autowired
    public RespuestaServiceImpl(RespuestaRepository respuestaRepository, PreguntaRepository preguntaRepository) {
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    @Override
    @Transactional
    public Respuesta crearRespuesta(String respuesta, String imagen, Boolean correcta, Long preguntaId) {
        
        Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta es incorrecta"));
        
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
        Respuesta respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
        respuestaObj.setRespuesta(respuesta);
        respuestaObj.setImagen(imagen);
        respuestaObj.setCorrecta(correcta);
        return respuestaRepository.save(respuestaObj);
    }

    @Override
    @Transactional
    public void deleteRespuesta(Long id) {
        Respuesta respuestaObj = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
        respuestaRepository.delete(respuestaObj);
    }
}
