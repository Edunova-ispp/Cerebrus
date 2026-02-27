package com.cerebrus.actividadalumno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaalumno.RespuestaAlumno;
import com.cerebrus.respuestaalumno.RespuestaAlumnoService;
import com.cerebrus.usuario.AlumnoRepository;

@Service
@Transactional
public class ActividadAlumnoServiceImpl implements ActividadAlumnoService {

    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final ActividadRepository actividadRepository;
    private final AlumnoRepository alumnoRepository;
    private final RespuestaAlumnoService respuestaAlumnoService;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository, RespuestaAlumnoService respuestaAlumnoService) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
        this.respuestaAlumnoService = respuestaAlumnoService;
    }

    @Override
    @Transactional
    public ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada, Integer nota, Integer numAbandonos, Long alumnoId, Long actId) {
        
        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        Alumno alumno = alumnoRepository.findById(alumnoId).orElseThrow(() -> new ResourceNotFoundException("El alumno no existe"));

        ActividadAlumno actividadAlumno = new ActividadAlumno(tiempo, puntuacion, 
            inicio, acabada, nota, numAbandonos, alumno, actividad);
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadAlumno readActividadAlumno(Long id) {
        return actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
    }

    @Override
    @Transactional
    public ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion,
         LocalDateTime inicio, LocalDateTime acabada, Integer nota, Integer numAbandonos) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        actividadAlumno.setTiempo(tiempo);
        actividadAlumno.setPuntuacion(puntuacion);
        actividadAlumno.setInicio(inicio);
        actividadAlumno.setAcabada(acabada);
        actividadAlumno.setNota(nota);
        actividadAlumno.setNumAbandonos(numAbandonos);
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional
    public void deleteActividadAlumno(Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        actividadAlumnoRepository.delete(actividadAlumno);
    }

    @Override
    @Transactional
    public ActividadAlumno corregirActividadAlumno(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        if (nuevaNota != null) {
            corregirNotaActividadAlumno(actividadAlumno, nuevaNota);
        }
        if (nuevasCorreccionesRespuestasIds != null && !nuevasCorreccionesRespuestasIds.isEmpty()) {
            corregirRespuestasActividadAlumno(actividadAlumno, nuevasCorreccionesRespuestasIds);
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    public void corregirNotaActividadAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota) {
        actividadAlumno.setNota(nuevaNota);
    }

    @Override
    public void corregirRespuestasActividadAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds) {
        for (Long respuestaId: nuevasCorreccionesRespuestasIds) {
            RespuestaAlumno respuestaAlumno = respuestaAlumnoService.encontrarRespuestaAlumnoPorId(respuestaId);
            if(respuestaAlumno == null) {
                throw new ResourceNotFoundException("RespuestaAlumno", "id", respuestaId);
            } else if (!respuestaAlumno.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
                throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no pertenece a la actividad del alumno con id " + actividadAlumno.getId());
            }
            respuestaAlumnoService.marcarODesmarcarRespuestaCorrecta(respuestaId);
        }
    }
}
