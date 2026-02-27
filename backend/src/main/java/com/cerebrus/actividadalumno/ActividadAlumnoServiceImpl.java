package com.cerebrus.actividadalumno;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.AlumnoRepository;

@Service
@Transactional
public class ActividadAlumnoServiceImpl implements ActividadAlumnoService {

    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final ActividadRepository actividadRepository;
    private final AlumnoRepository alumnoRepository;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    @Transactional
    public ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDate fecha, LocalDate inicio,
        LocalDate acabada, Integer nota, Integer numAbandonos, Long alumnoId, Long actId) {
        
        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        Alumno alumno = alumnoRepository.findById(alumnoId).orElseThrow(() -> new ResourceNotFoundException("El alumno no existe"));

        ActividadAlumno actividadAlumno = new ActividadAlumno(tiempo, puntuacion, fecha, 
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
    public ActividadAlumno updateActividadAlumno(Long id, Integer tiempo, Integer puntuacion, LocalDate fecha,
         LocalDate inicio, LocalDate acabada, Integer nota, Integer numAbandonos) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        actividadAlumno.setTiempo(tiempo);
        actividadAlumno.setPuntuacion(puntuacion);
        actividadAlumno.setFecha(fecha);
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
}
