package com.cerebrus.actividad;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;

import com.cerebrus.pregunta.PreguntaRepository;

@Service
@Transactional
public class GeneralServiceImpl implements GeneralService {

    private final GeneralRepository generalRepository;
    private final TemaRepository temaRepository;
    private final PreguntaRepository preguntaRepository;

    @Autowired
    public GeneralServiceImpl(GeneralRepository generalRepository, TemaRepository temaRepository, PreguntaRepository preguntaRepository) {
        this.generalRepository = generalRepository;
        this.temaRepository = temaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    @Override
    public General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        List<Long> preguntasId) {

        Tema tema = temaRepository.findById(temaId).orElse(null);
        List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);

        General tipoTest = new General();
        tipoTest.setTitulo(titulo);
        tipoTest.setDescripcion(descripcion);
        tipoTest.setPuntuacion(puntuacion);
        tipoTest.setTema(tema);
        tipoTest.setPreguntas(preguntas);
        tipoTest.setTipo(TipoActGeneral.TEST);
        
        return generalRepository.save(tipoTest);
    }
}
