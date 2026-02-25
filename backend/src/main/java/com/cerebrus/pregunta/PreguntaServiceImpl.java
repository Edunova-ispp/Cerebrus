package com.cerebrus.pregunta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.utils.CerebrusUtils;

@Service
@Transactional
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final ActividadRepository actividadRepository;

    @Autowired
    public PreguntaServiceImpl(PreguntaRepository preguntaRepository, ActividadRepository actividadRepository) {
        this.preguntaRepository = preguntaRepository;
        this.actividadRepository = actividadRepository;
    }

    @Override
    @Transactional
    public Pregunta crearPregunta(String pregunta, String imagen, Long actId) {
        
        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new RuntimeException("La actividad es incorrecta"));
        
        Pregunta preguntaObj = new Pregunta();
        preguntaObj.setPregunta(pregunta);
        preguntaObj.setImagen(imagen);
        preguntaObj.setActividad(actividad);
        return preguntaRepository.save(preguntaObj);
    }

    @Override
    public Pregunta readPregunta(Long id) {
        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
        List<Respuesta> respuestas = preguntaObj.getRespuestas();
        List<Respuesta> respuestasDesordenadas = CerebrusUtils.shuffleCollection(respuestas).stream().toList();
        preguntaObj.setRespuestas(respuestasDesordenadas);
        return preguntaObj;
    }

    @Override
    @Transactional
    public Pregunta updatePregunta(Long id, String pregunta, String imagen) {
        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
        preguntaObj.setPregunta(pregunta);
        preguntaObj.setImagen(imagen);
        return preguntaRepository.save(preguntaObj);
    }

    @Override
    @Transactional
    public void deletePregunta(Long id) {
        Pregunta preguntaObj = preguntaRepository.findById(id).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
        preguntaRepository.delete(preguntaObj);
    }
}
