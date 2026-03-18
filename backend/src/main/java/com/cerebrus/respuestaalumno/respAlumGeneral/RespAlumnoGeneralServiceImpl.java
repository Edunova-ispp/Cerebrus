package com.cerebrus.respuestaAlumno.respAlumGeneral;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadAlumno.ActividadAlumno;
import com.cerebrus.actividadAlumno.ActividadAlumnoRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.respuestaMaestro.RespuestaMaestroService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;


@Service
@Transactional
public class RespAlumnoGeneralServiceImpl implements RespAlumnoGeneralService {

    private final RespAlumnoGeneralRepository respAlumnoGeneralRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaMaestroRepository respuestaRepository;
    private final RespuestaMaestroService respuestaService;
    private final UsuarioService usuarioService;
    private final ActividadRepository actividadRepository;

    @Autowired
    public RespAlumnoGeneralServiceImpl(RespAlumnoGeneralRepository respAlumnoGeneralRepository, 
        ActividadAlumnoRepository actividadAlumnoRepository, PreguntaRepository preguntaRepository, 
        RespuestaMaestroRepository respuestaRepository, RespuestaMaestroService respuestaService, UsuarioService usuarioService, ActividadRepository actividadRepository) {
        this.respAlumnoGeneralRepository = respAlumnoGeneralRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.respuestaService = respuestaService;
        this.usuarioService = usuarioService;
        this.actividadRepository = actividadRepository;
    }

    @Override
    @Transactional
    public RespAlumnoGeneralCreateResponse crearRespAlumnoGeneral(Long actAlumnoId, Long respuestaId, Long preguntaId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas de alumno");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actAlumnoId).orElseThrow(() -> new RuntimeException("La actividad del alumno no existe"));
        Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
        RespuestaMaestro respuestaObj = respuestaRepository.findById(respuestaId).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
        Boolean correcta = respuestaObj.getCorrecta();
        String respuestaTexto = respuestaObj.getRespuesta();
        String comentariosRespVisible;
        if (pregunta.getActividad().getRespVisible()) {
            comentariosRespVisible = pregunta.getActividad().getComentariosRespVisible();
        } else {
            comentariosRespVisible = "";
        }

        RespAlumnoGeneral respAlumnoGeneralEntity = new RespAlumnoGeneral(correcta, actividadAlumno, respuestaTexto, pregunta);
    
    // IMPORTANTE: Guardamos y capturamos la entidad persistida (que ya tiene ID)
    RespAlumnoGeneral guardada = respAlumnoGeneralRepository.save(respAlumnoGeneralEntity);

    // Pasamos guardada.getId() al constructor
    return new RespAlumnoGeneralCreateResponse(guardada.getId(), correcta, comentariosRespVisible);
}

    @Override
    @Transactional(readOnly = true)
    public RespAlumnoGeneral readRespAlumnoGeneral(Long id) {
        return respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
    }

// ESTOS MÉTODOS QUEDAN DEFINIDOS POR SI ES NECESARIO UTILIZARLOS, 
// PERO PARA LA FEATURE 35 NO SON NECESARIOS (POR ELLO POR AHORA NO HAY VALIDACIÓN DE ROLES)

    @Override
    @Transactional
    public RespAlumnoGeneral updateRespAlumnoGeneral(Long id,Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        respAlumnoGeneral.setCorrecta(correcta);
        respAlumnoGeneral.setRespuesta(respuesta);
        respAlumnoGeneral.setPregunta(preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe")));
        return respAlumnoGeneralRepository.save(respAlumnoGeneral);
    }

    @Override
    @Transactional
    public void deleteRespAlumnoGeneral(Long id) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        respAlumnoGeneralRepository.delete(respAlumnoGeneral);
    }

    @Override
    public Boolean corregirRespuestaAlumnoGeneral(Long id) {
        RespAlumnoGeneral respuestaAlumno = respAlumnoGeneralRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumnoGeneral", "id", id));
        List<RespuestaMaestro> respuestas = respuestaService.encontrarRespuestasPorPreguntaId(respuestaAlumno.getPregunta().getId());
        Boolean esCorrecta = false;

        for (RespuestaMaestro r : respuestas) {
            if (r.getRespuesta().equals(respuestaAlumno.getRespuesta()) && r.getCorrecta()) {
                respuestaAlumno.setCorrecta(r.getCorrecta());
                respAlumnoGeneralRepository.save(respuestaAlumno);
                esCorrecta = r.getCorrecta();
                break;
            }
        }

        return esCorrecta;
    }

    public boolean corregirRespuestaAlumnoGeneralTest(Long id) {
        RespAlumnoGeneral respuestaAlumno = respAlumnoGeneralRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumnoGeneral", "id", id));
        List<RespuestaMaestro> respuestas = respuestaService.encontrarRespuestasPorPreguntaId(respuestaAlumno.getPregunta().getId());
        Boolean esCorrecta = false;
        System.out.println("Respuesta del alumno: " + respuestaAlumno.getRespuesta());
        for (RespuestaMaestro r : respuestas) {
                System.out.println("Respuesta correcta: " + r.getRespuesta() + " - Correcta: " + r.getCorrecta());
                if(r.getRespuesta().equals(respuestaAlumno.getRespuesta()) ) {
                   esCorrecta = true;
                }
        }
        
           
        

        return esCorrecta;
    }

    @Override
    @Transactional
    public HashMap<Long, String> corregirCrucigrama(LinkedHashMap<Long, String> respuestas, Long crucigramaId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede responder crucigramas");
        }

        Alumno alumno = (Alumno) u;
        Integer nota = 0;
        Integer puntuacion = 0;
        Integer puntuacionASumar = actividadRepository.findById(crucigramaId).orElseThrow(() -> new RuntimeException("El crucigrama no existe")).getPuntuacion() / respuestas.size();
        List<RespAlumnoGeneral> respuestasAlumno = new java.util.ArrayList<>();
        ActividadAlumno  actividadAlumno = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumno.getId(), crucigramaId)
            .orElse(actividadAlumnoRepository.save(new ActividadAlumno(0,LocalDateTime.now(),null,0,0,alumno,actividadRepository.findByID(crucigramaId))));
        HashMap<Long, String> resultado = new HashMap<>();

        for(Entry<Long, String> entry : respuestas.entrySet()) {
            Long preguntaId = entry.getKey();
            String respuestaDada = entry.getValue().strip().toLowerCase();

            Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
            if (!pregunta.getActividad().getId().equals(crucigramaId)) {
                throw new IllegalArgumentException("La pregunta con id " + preguntaId + " no pertenece al crucigrama con id " + crucigramaId);
            }

            RespuestaMaestro respuestaCorrecta = respuestaService.encontrarRespuestasPorPreguntaId(preguntaId).stream()
                .filter(RespuestaMaestro::getCorrecta)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró una respuesta correcta para la pregunta con id " + preguntaId));

            Boolean esCorrecta = respuestaDada.equals(respuestaCorrecta.getRespuesta().strip().toLowerCase());

            if (esCorrecta) {
                nota += 10/respuestas.size();
                puntuacion += puntuacionASumar;
            }
            else {
                nota -= 10/(respuestas.size()*4);
                puntuacion -= puntuacionASumar/4;
            }

            if(pregunta.getActividad().getRespVisible()) {
                resultado.put(preguntaId, esCorrecta ? " Respuesta Correcta" : "Incorrecta la respuesta correcta era: " + respuestaCorrecta.getRespuesta());
            } else {
                resultado.put(preguntaId, esCorrecta ? "Respuesta Correcta" : " Respuesta Incorrecta");
            }

            RespAlumnoGeneral respAlumnoGeneral = new RespAlumnoGeneral(esCorrecta, null, respuestaDada, pregunta);
            respAlumnoGeneral.setActividadAlumno(actividadAlumno);
            respAlumnoGeneral = respAlumnoGeneralRepository.save(respAlumnoGeneral);
            respuestasAlumno.add(respAlumnoGeneral);
        }
        if(puntuacion < 0) {
            puntuacion = 0;
        }
        if(nota < 0) {
            nota = 0;
        }
        actividadAlumno.setNota(nota);
        actividadAlumno.setPuntuacion(puntuacion);
        actividadAlumno.setFechaFin(LocalDateTime.now());
        actividadAlumnoRepository.save(actividadAlumno);
        resultado.put(-1L, "Nota final: " + nota + " - Puntuación final: " + puntuacion);
        return resultado;
    
}
}
