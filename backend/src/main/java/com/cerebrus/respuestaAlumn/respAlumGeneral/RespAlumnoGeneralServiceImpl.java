package com.cerebrus.respuestaAlumn.respAlumGeneral;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.iaconnection.IaConnectionService;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaRequest;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;
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
    private final IaConnectionService iaConnectionService;

    @Autowired
    public RespAlumnoGeneralServiceImpl(RespAlumnoGeneralRepository respAlumnoGeneralRepository, 
        ActividadAlumnoRepository actividadAlumnoRepository, PreguntaRepository preguntaRepository, 
        RespuestaMaestroRepository respuestaRepository, RespuestaMaestroService respuestaService, UsuarioService usuarioService, ActividadRepository actividadRepository,
        IaConnectionService iaConnectionService) {
        this.respAlumnoGeneralRepository = respAlumnoGeneralRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.respuestaService = respuestaService;
        this.usuarioService = usuarioService;
        this.actividadRepository = actividadRepository;
        this.iaConnectionService = iaConnectionService;
    }

    @Override
    @Transactional
    public RespAlumnoGeneralCreateResponse crearRespuestaAlumnoGeneral(Long actAlumnoId, Long respuestaId, Long preguntaId) {

        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas de alumno");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actAlumnoId).orElseThrow(() -> new RuntimeException("La actividad del alumno no existe"));
        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes crear una respuesta para una ActividadAlumno que no es tuya");
        }

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
    public RespAlumnoGeneral encontrarRespuestaAlumnoGeneralPorId(Long id) {
        return respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
    }

// ESTOS MÉTODOS QUEDAN DEFINIDOS POR SI ES NECESARIO UTILIZARLOS, 
// PERO PARA LA FEATURE 35 NO SON NECESARIOS (POR ELLO POR AHORA NO HAY VALIDACIÓN DE ROLES)

    @Override
    @Transactional
    public RespAlumnoGeneral actualizarRespuestaAlumnoGeneral(Long id,Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede modificar una respuesta alumno");
        }
        if (respAlumnoGeneral.getActividadAlumno().getAlumno() == null || respAlumnoGeneral.getActividadAlumno().getAlumno().getId() == null
            || !respAlumnoGeneral.getActividadAlumno().getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes modificar una respuesta alumno que no es tuya");
        }
        respAlumnoGeneral.setCorrecta(correcta);
        respAlumnoGeneral.setRespuesta(respuesta);
        respAlumnoGeneral.setPregunta(preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe")));
        return respAlumnoGeneralRepository.save(respAlumnoGeneral);
    }

    @Override
    @Transactional
    public void eliminarRespuestaAlumnoGeneralPorId(Long id) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede eliminar una respuesta alumno");
        }
        if (respAlumnoGeneral.getActividadAlumno().getAlumno() == null || respAlumnoGeneral.getActividadAlumno().getAlumno().getId() == null
            || !respAlumnoGeneral.getActividadAlumno().getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes eliminar una respuesta alumno que no es tuya");
        }
        respAlumnoGeneralRepository.delete(respAlumnoGeneral);
    }

    @Override
    public Boolean corregirRespuestaAlumnoGeneral(Long id) {
        RespAlumnoGeneral respuestaAlumno = respAlumnoGeneralRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumnoGeneral", "id", id));
        List<RespuestaMaestro> respuestas = respuestaService.encontrarRespuestasMaestroPorPreguntaId(respuestaAlumno.getPregunta().getId());
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

    public boolean corregirRespuestaAlumnoGeneralClasificacion(Long id) {
        RespAlumnoGeneral respuestaAlumno = respAlumnoGeneralRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumnoGeneral", "id", id));
        List<RespuestaMaestro> respuestas = respuestaService.encontrarRespuestasMaestroPorPreguntaId(respuestaAlumno.getPregunta().getId());
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
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede responder crucigramas");
        }

        Alumno alumno = (Alumno) current;
        Integer nota = 0;
        Integer puntuacion = 0;
        Integer puntuacionASumar = actividadRepository.findById(crucigramaId).orElseThrow(() -> new ResourceNotFoundException("El crucigrama no existe")).getPuntuacion() / respuestas.size();
        List<RespAlumnoGeneral> respuestasAlumno = new java.util.ArrayList<>();
        // Buscar si ya existe ActividadAlumno para este alumno y actividad
        // Si NO existe, crear UNA NUEVA
        // Si existe, reutilizar la misma y actualizar datos
        ActividadAlumno actividadAlumno;
        var existente = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumno.getId(), crucigramaId);
        if (existente.isPresent()) {
            // Ya existe: usar la misma
            actividadAlumno = existente.get();
        } else {
            // NO existe: crear una nueva
            actividadAlumno = actividadAlumnoRepository.save(new ActividadAlumno(0, LocalDateTime.now(), null, 0, 0, alumno, actividadRepository.findByID(crucigramaId)));
        }

        HashMap<Long, String> resultado = new HashMap<>();
        for(Entry<Long, String> entry : respuestas.entrySet()) {
            Long preguntaId = entry.getKey();
            String respuestaDada = entry.getValue().strip().toLowerCase();

            
            Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
            if (!pregunta.getActividad().getId().equals(crucigramaId)) {
                throw new IllegalArgumentException("La pregunta con id " + preguntaId + " no pertenece al crucigrama con id " + crucigramaId);
            }
            
            RespuestaMaestro respuestaCorrecta = respuestaService.encontrarRespuestasMaestroPorPreguntaId(preguntaId).stream()
                .filter(RespuestaMaestro::getCorrecta)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró una respuesta correcta para la pregunta con id " + preguntaId));

            Boolean esCorrecta = respuestaDada.equals(respuestaCorrecta.getRespuesta().strip().toLowerCase());

            if (esCorrecta) {
                nota += 10/respuestas.size();
                puntuacion += puntuacionASumar;
            }
            else {
                nota -= 10/(respuestas.size()*2);
                puntuacion -= puntuacionASumar/2;
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

    @Override
    @Transactional
    public EvaluacionActividadAbiertaResponse corregirActividadAbierta(EvaluacionActividadAbiertaRequest request) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede enviar respuestas de la actividad");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(request.getActividadAlumnoId())
            .orElseThrow(() -> new RuntimeException("La actividad del alumno no existe"));

        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes enviar respuestas a una corrección automática una Actividad Alumno que no es tuya");
        }

        General actividad = (General) actividadAlumno.getActividad();

        if (actividad.getTipo() != TipoActGeneral.ABIERTA) {
            throw new IllegalArgumentException("La actividad no es de tipo abierta");
        }

        LinkedHashMap<Long, String> respuestas = request.getRespuestasAlumno();

        int puntuacionTotal = 0;
        int numPreguntas = respuestas.size();
        
        int maxPuntuacionPorPregunta = (actividad.getPuntuacion() != null && actividad.getPuntuacion() > 0) 
                                        ? actividad.getPuntuacion() / numPreguntas : 100 / numPreguntas;

        List<RespAlumnoAbiertaResponse> detalles = new java.util.ArrayList<>();
        Boolean isRespVisible = Boolean.TRUE.equals(actividad.getRespVisible());
        String comentariosRespVisible = (isRespVisible && actividad.getComentariosRespVisible() != null) 
                                        ? actividad.getComentariosRespVisible() : "";

        for (Map.Entry<Long, String> entry : respuestas.entrySet()) {
            Long preguntaId = entry.getKey();
            String respuestaDada = entry.getValue();

            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RuntimeException("La pregunta " + preguntaId + " no existe"));
            
            if (!pregunta.getActividad().getId().equals(actividad.getId())) {
                throw new IllegalArgumentException("La pregunta " + preguntaId + " no pertenece a la actividad");
            }

            RespuestaMaestro modeloRespuesta = respuestaService.encontrarRespuestasMaestroPorPreguntaId(preguntaId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró modelo de respuesta para la pregunta"));

            Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta.getPregunta(),
                respuestaDada,
                modeloRespuesta.getRespuesta(),
                maxPuntuacionPorPregunta 
            );

            Integer puntuacionPregunta = 0;
            String comentariosIA = "La IA no devolvió comentarios legibles";

            for (Map.Entry<String, Object> evalEntry : evaluacion.entrySet()) {
                String clave = evalEntry.getKey().toLowerCase();
                if (clave.contains("puntuacion") || clave.contains("puntuación")) {
                    puntuacionPregunta = Integer.valueOf(evalEntry.getValue().toString());
                }
                if (clave.contains("comentario")) {
                    comentariosIA = evalEntry.getValue().toString();
                }
            }

            if (puntuacionPregunta > maxPuntuacionPorPregunta) puntuacionPregunta = maxPuntuacionPorPregunta;
            else if (puntuacionPregunta < 0) puntuacionPregunta = 0;

            puntuacionTotal += puntuacionPregunta;

            RespAlumnoGeneral respAlumnoGeneralEntity = new RespAlumnoGeneral(puntuacionPregunta > 0, actividadAlumno, respuestaDada, pregunta);
            RespAlumnoGeneral guardada = respAlumnoGeneralRepository.save(respAlumnoGeneralEntity);

            String comentarioMostrar = isRespVisible ? comentariosIA : "Corrección oculta por configuración de la actividad.";

            detalles.add(new RespAlumnoAbiertaResponse(
                guardada.getId(),
                puntuacionPregunta,
                comentarioMostrar,
                isRespVisible,
                comentariosRespVisible
            ));
        }

        int maxPuntuacionTotal = (actividad.getPuntuacion() != null && actividad.getPuntuacion() > 0) ? actividad.getPuntuacion() : 100;
        Integer notaFinal = Math.round(((float) puntuacionTotal / maxPuntuacionTotal) * 10);
        
        if (notaFinal > 10) notaFinal = 10;
        else if (notaFinal < 0) notaFinal = 0;

        actividadAlumno.setPuntuacion(puntuacionTotal);
        actividadAlumno.setNota(notaFinal);
        actividadAlumno.setFechaFin(LocalDateTime.now());
        actividadAlumnoRepository.save(actividadAlumno);

        return new EvaluacionActividadAbiertaResponse(notaFinal, puntuacionTotal, detalles);
    }
}