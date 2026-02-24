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
    public General crearActGeneral(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible) {

        Tema tema = temaRepository.findById(temaId).orElseThrow(() -> new RuntimeException("El tema es incorrecto"));

        General actividad = new General();
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setPuntuacion(puntuacion);
        if(respVisible.equals(Boolean.TRUE)){
            actividad.setRespVisible(true);
            actividad.setComentariosRespVisible(comentariosRespVisible);
        }
        actividad.setVersion(1);
        actividad.setPosicion(tema.getActividades().size());
        actividad.setTema(tema);
        return actividad;
    }

    @Override
    @Transactional
    public General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible,
            List<Long> preguntasId) {
        General tipoTest = crearActGeneral(titulo, descripcion, puntuacion, temaId, respVisible, comentariosRespVisible);
        List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
        tipoTest.setPreguntas(preguntas);
        tipoTest.setTipo(TipoActGeneral.TEST);
        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional
    public General readActividad(Long id){
        return generalRepository.findByIdWithPreguntas(id).orElseThrow(() -> new RuntimeException("TipoTest no encontrado"));
    }

    @Override
    public General updateActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible){
        General actividad = generalRepository.findById(id).orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setPuntuacion(puntuacion);
        if(respVisible.equals(Boolean.FALSE)){
            actividad.setRespVisible(false);
            actividad.setComentariosRespVisible(null);
        }
        if(comentariosRespVisible.isBlank() || comentariosRespVisible.isEmpty()){
            actividad.setComentariosRespVisible(null);
        } else {
            actividad.setComentariosRespVisible(comentariosRespVisible);
        }
        actividad.setVersion(2);
        //TODO: Posicion y tema cambian???
        return actividad;
    }

    @Override
    @Transactional
    public General updateTipoTest(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId){
     
        General tipoTest = updateActGeneral(id, titulo, descripcion, puntuacion, respVisible, comentariosRespVisible);
        if(preguntasId != null){
            List<Pregunta> preguntas = preguntaRepository.findAllById(preguntasId);
            tipoTest.getPreguntas().clear();
            tipoTest.getPreguntas().addAll(preguntas);
        }

        return generalRepository.save(tipoTest);
    }

    @Override
    @Transactional
    public void deleteActividad(Long id) {  
        General general = generalRepository.findById(id).orElseThrow(() -> new RuntimeException("Actividad tipo test no encontrada"));
        generalRepository.delete(general);
    }
}
