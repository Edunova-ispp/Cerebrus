package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.General;
import com.cerebrus.actividad.Ordenacion;
import com.cerebrus.actividad.OrdenacionService;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralService;
import com.cerebrus.respuestaalumno.RespAlumnoOrdenacionService;
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
    private final RespAlumnoGeneralService respAlumnoGeneralService;
    private final OrdenacionService ordenacionService; 
    private final RespAlumnoOrdenacionService respAlumnoOrdenacionService;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository, RespuestaAlumnoService respuestaAlumnoService,
        RespAlumnoGeneralService respAlumnoGeneralService, OrdenacionService ordenacionService, RespAlumnoOrdenacionService respAlumnoOrdenacionService) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
        this.respuestaAlumnoService = respuestaAlumnoService;
        this.respAlumnoGeneralService = respAlumnoGeneralService;
        this.ordenacionService = ordenacionService;
        this.respAlumnoOrdenacionService = respAlumnoOrdenacionService;
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
    public ActividadAlumno corregirActividadAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds) {
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

    @Override
    @Transactional
    public ActividadAlumno corregirActividadAlumnoAutomaticamente(Long id, List<Long> respuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        Actividad actividad = actividadAlumno.getActividad();
        if(actividad instanceof General) {
            corregirActividadAlumnoAutomaticamenteGeneral(actividadAlumno, respuestasIds, actividad);
        } else if(actividad instanceof Ordenacion) {
            corregirActividadAlumnoAutomaticamenteOrdenacion(actividadAlumno, respuestasIds, actividad);
        } else {
            throw new IllegalArgumentException("Tipo de actividad no soportado para corrección automática");
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        Integer puntuacionTotal = actividad.getPuntuacion();
        Integer notaTotal = 10;
        Integer puntuacionFinal = 0;
        Integer notaFinal = 0;
        Integer numRespuestas = respuestasIds.size();
        Integer puntuacionPorRespuesta = numRespuestas > 0 ? puntuacionTotal / numRespuestas : 0;
        Integer notaPorRespuesta = numRespuestas > 0 ? notaTotal / numRespuestas : 0;
        for (Long respuestaId: respuestasIds) {
            RespuestaAlumno respuestaAlumno = respuestaAlumnoService.encontrarRespuestaAlumnoPorId(respuestaId);
            if(respuestaAlumno == null) {
                throw new ResourceNotFoundException("RespuestaAlumno", "id", respuestaId);
            } else if (!respuestaAlumno.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
                throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no pertenece a la actividad del alumno con id " + actividadAlumno.getId());
            }
            boolean esCorrecta = respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(respuestaId);
            if (esCorrecta) {
                puntuacionFinal += puntuacionPorRespuesta;
                notaFinal += notaPorRespuesta;
            }
        }
        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteOrdenacion(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        Ordenacion actividadOrdenacion = ordenacionService.encontrarOrdenacionPorId(actividad.getId());
        Integer puntuacionTotal = actividad.getPuntuacion();
        Integer notaTotal = 10;
        Integer puntuacionFinal = 0;
        Integer notaFinal = 0;
        Integer numValores = actividadOrdenacion.getValores().size();
        Integer puntuacionPorRespuesta = numValores > 0 ? puntuacionTotal / numValores : 0;
        Integer notaPorRespuesta = numValores > 0 ? notaTotal / numValores : 0;

        if(respuestasIds.size() > 1) {
            throw new IllegalArgumentException("Para actividades de ordenación solo se permite una respuesta del alumno con la secuencia ordenada");
        }
        RespuestaAlumno respuestaAlumno = respuestaAlumnoService.encontrarRespuestaAlumnoPorId(respuestasIds.get(0));
        if(respuestaAlumno == null) {
            throw new ResourceNotFoundException("RespuestaAlumno", "id", respuestasIds.get(0));
        } else if (!respuestaAlumno.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
            throw new IllegalArgumentException("La respuesta con id " + respuestasIds.get(0) + " no pertenece a la actividad del alumno con id " + actividadAlumno.getId());
        }
        respAlumnoOrdenacionService.corregirRespuestaAlumnoOrdenacion(respuestasIds.get(0));
        Integer numPosicionesCorrectas = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(respuestasIds.get(0));
        puntuacionFinal = puntuacionPorRespuesta * numPosicionesCorrectas;
        notaFinal = notaPorRespuesta * numPosicionesCorrectas;
        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }
}
