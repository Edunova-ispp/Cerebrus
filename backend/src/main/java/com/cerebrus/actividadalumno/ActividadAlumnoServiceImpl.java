package com.cerebrus.actividadalumno;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.AlumnoRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
@Transactional
public class ActividadAlumnoServiceImpl implements ActividadAlumnoService {

    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final ActividadRepository actividadRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository, UsuarioService usuarioService) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public ActividadAlumno crearActividadAlumno(Integer tiempo, Integer puntuacion, LocalDateTime inicio,
        LocalDateTime acabada, Integer nota, Integer numAbandonos, Long alumnoId, Long actId) {

        // Idempotente: si ya existe la pareja (alumno, actividad), devolvemos la existente.
        Optional<ActividadAlumno> existing = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
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
    @Transactional(readOnly = true)
    public Optional<ActividadAlumno> readActividadAlumnoByAlumnoIdAndActividadId(Long alumnoId, Long actividadId) {
        return actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actividadId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer ensureActividadAlumno(Long actividadId) {
        // Devuelve 1 si existe ActividadAlumno para el alumno autenticado y actividadId; si no 0.
        // No debe lanzar error si no existe.
        try {
            Usuario current = usuarioService.findCurrentUser();
            if (current == null || current.getId() == null) {
                return 0;
            }
            Long alumnoId = current.getId();
            return actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actividadId).isPresent() ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
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

}
