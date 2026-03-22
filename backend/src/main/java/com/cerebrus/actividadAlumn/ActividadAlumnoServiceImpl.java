package com.cerebrus.actividadAlumn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.comun.enumerados.*;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionService;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.inscripcion.InscripcionRepository;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.respuestaAlumn.RespuestaAlumnoService;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralService;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacionService;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagenService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.Maestro;

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
    private final UsuarioService usuarioService;
    private final MarcarImagenService marcarImagenService;
    private final RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;
    private final InscripcionRepository inscripcionRepository;

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository, RespuestaAlumnoService respuestaAlumnoService,
        RespAlumnoGeneralService respAlumnoGeneralService, OrdenacionService ordenacionService, 
        RespAlumnoOrdenacionService respAlumnoOrdenacionService, UsuarioService usuarioService, 
        MarcarImagenService marcarImagenService, RespAlumnoPuntoImagenService respAlumnoPuntoImagenService,
        InscripcionRepository inscripcionRepository) {
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.actividadRepository = actividadRepository;
        this.alumnoRepository = alumnoRepository;
        this.respuestaAlumnoService = respuestaAlumnoService;
        this.respAlumnoGeneralService = respAlumnoGeneralService;
        this.ordenacionService = ordenacionService;
        this.respAlumnoOrdenacionService = respAlumnoOrdenacionService;
        this.usuarioService = usuarioService;
        this.marcarImagenService = marcarImagenService;
        this.respAlumnoPuntoImagenService = respAlumnoPuntoImagenService;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    @Transactional
    public ActividadAlumno crearActividadAlumno(Integer puntuacion, LocalDateTime fechaInicio,
        LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Long alumnoId, Long actId) {

        if (alumnoId == null || actId == null) {
            throw new IllegalArgumentException("El alumno y la actividad son obligatorios");
        }

        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede iniciar actividades");
        }

        if (!current.getId().equals(alumnoId)) {
            throw new AccessDeniedException("No puedes iniciar actividades para otro alumno");
        }

        // Idempotente: si ya existe la pareja (alumno, actividad), devolvemos la existente.
        Optional<ActividadAlumno> existing = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        Alumno alumno = alumnoRepository.findById(alumnoId).orElseThrow(() -> new ResourceNotFoundException("El alumno no existe"));

        LocalDate fechaActividad = (fechaInicio == null ? LocalDate.now() : fechaInicio.toLocalDate());
        validarInscripcionPrevia(alumnoId, actividad, fechaActividad);

        ActividadAlumno actividadAlumno = new ActividadAlumno(puntuacion, 
            fechaInicio, fechaFin, nota, numAbandonos, alumno, actividad);

        return actividadAlumnoRepository.save(actividadAlumno);
    }

    private void validarInscripcionPrevia(Long alumnoId, Actividad actividad, LocalDate fechaActividad) {
        Long cursoId = actividad.getTema().getCurso().getId();
        Inscripcion inscripcion = inscripcionRepository.findByAlumnoIdAndCursoId(alumnoId, cursoId);
        if (inscripcion == null) {
            throw new AccessDeniedException("El alumno no esta inscrito en el curso de la actividad");
        }
        if (inscripcion.getFechaInscripcion() != null && inscripcion.getFechaInscripcion().isAfter(fechaActividad)) {
            throw new AccessDeniedException("No puedes realizar la actividad antes de la fecha de inscripcion");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadAlumno readActividadAlumno(Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        validarAccesoAlumnoOMaestroPropietario(actividadAlumno);
        return actividadAlumno;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActividadAlumno> readActividadAlumnoByAlumnoIdAndActividadId(Long alumnoId, Long actividadId) {
        Usuario current = usuarioService.findCurrentUser();
        if (current instanceof Alumno) {
            if (!current.getId().equals(alumnoId)) {
                throw new AccessDeniedException("No puedes consultar actividades de otro alumno");
            }
        } else if (current instanceof Maestro) {
            Actividad actividad = actividadRepository.findById(actividadId)
                    .orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
            Long maestroCursoId = actividad.getTema().getCurso().getMaestro().getId();
            if (maestroCursoId == null || !maestroCursoId.equals(current.getId())) {
                throw new AccessDeniedException("Solo el maestro propietario puede consultar esta actividad");
            }
        } else {
            throw new AccessDeniedException("No tienes permisos para consultar actividades de alumnos");
        }
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
    public ActividadAlumno updateActividadAlumno(Long id, Integer puntuacion,
         LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer nota, Integer numAbandonos) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        validarAccesoAlumnoOMaestroPropietario(actividadAlumno);
        actividadAlumno.setPuntuacion(puntuacion);
        actividadAlumno.setFechaInicio(fechaInicio);
        actividadAlumno.setFechaFin(fechaFin);
        actividadAlumno.setNota(nota);
        actividadAlumno.setNumAbandonos(numAbandonos);
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional
    public void deleteActividadAlumno(Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        validarAccesoAlumnoOMaestroPropietario(actividadAlumno);
        actividadAlumnoRepository.delete(actividadAlumno);
    }

    @Override
    @Transactional
    public ActividadAlumno abandonarActividadAlumno(Long actividadAlumnoId) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede abandonar su actividad");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actividadAlumnoId)
            .orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));

        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes modificar una ActividadAlumno que no es tuya");
        }

        // Si ya está acabada, no contamos abandono.
        if (actividadAlumno.getEstadoActividad() == EstadoActividad.TERMINADA) {
            return actividadAlumno;
        }

        Integer prev = actividadAlumno.getNumAbandonos() == null ? 0 : actividadAlumno.getNumAbandonos();
        actividadAlumno.setNumAbandonos(prev + 1);
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional
    public ActividadAlumno corregirActividadAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        validarAccesoMaestroPropietario(actividadAlumno);
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
        validarAccesoAlumnoOMaestroPropietario(actividadAlumno);
        Actividad actividad = actividadAlumno.getActividad();
        LocalDateTime finalActividad = LocalDateTime.now();
        actividadAlumno.setFechaFin(finalActividad);
        if(actividad instanceof General){
            if (((General) actividad).getTipo() == TipoActGeneral.TEST) {
                corregirActividadAlumnoAutomaticamenteGeneral(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.CARTA){
                corregirActividadAlumnoAutomaticamenteCartaGeneral(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.TEORIA){
              // CASO PARA TEORÍA: Simplemente marcamos como terminada y damos la puntuación base
               actividadAlumno.setPuntuacion(actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 1);
               actividadAlumno.setNota(10); // Nota máxima por leer
               actividadAlumno.setFechaFin(LocalDateTime.now());
            } else if (((General) actividad).getTipo() == TipoActGeneral.CRUCIGRAMA){
                corregirActividadAlumnoAutomaticamenteCrucigrama(actividadAlumno, actividad);
            }
        } else if(actividad instanceof Ordenacion) {
            corregirActividadAlumnoAutomaticamenteOrdenacion(actividadAlumno, respuestasIds, actividad);
        } else if(actividad instanceof MarcarImagen){
            corregirActividadAlumnoAutomaticamenteMarcarImagen(actividadAlumno, respuestasIds, actividad);
        } else {    
        // CASO PARA TEORÍA: se ha movido dentro de instancia de GENERAL porque la actividad de teoria es una actividad general
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        // 1. Obtenemos el número REAL de preguntas que tiene el test
        // Usamos la colección de la entidad actividad
        General actividadGeneral = (General) actividad;
        int numPreguntasTotales = actividadGeneral.getPreguntas().size();    
        if (numPreguntasTotales == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return;
        }

        int puntuacionMaximaActividad = (actividad.getPuntuacion() != null) ? actividad.getPuntuacion() : 0;
        double notaMaxima = 10.0;
        
        // 2. Calculamos el valor de CADA pregunta basándonos en el TOTAL del test
        double valorPuntoPorPregunta = (double) puntuacionMaximaActividad / numPreguntasTotales;
        double valorNotaPorPregunta = notaMaxima / numPreguntasTotales;

        double puntuacionAcumulada = 0;
        double notaAcumulada = 0;

        if (respuestasIds != null) {
            for (Long respuestaId : respuestasIds) {
                // Este método DEBE comparar la respuesta del alumno con la correcta en la DB
                boolean esCorrecta = respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(respuestaId);
                
                if (esCorrecta) {
                    puntuacionAcumulada += valorPuntoPorPregunta;
                    notaAcumulada += valorNotaPorPregunta;
                }
                else {
                    puntuacionAcumulada -= valorPuntoPorPregunta / 2;
                    notaAcumulada -= valorNotaPorPregunta / 2;
                    
                }
            }
        }

    // 3. Guardar y redondear
    if(puntuacionAcumulada < 0) {
        puntuacionAcumulada = 0;
    }
    if(notaAcumulada < 0) {
        notaAcumulada = 0;
    }
    actividadAlumno.setPuntuacion((int) Math.round(puntuacionAcumulada));
    actividadAlumno.setNota((int) Math.round(notaAcumulada));
    actividadAlumno.setFechaFin(LocalDateTime.now());
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteCartaGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        General actividadGeneral = (General) actividad;
        int totalPreguntas = actividadGeneral.getPreguntas().size();

        if (totalPreguntas == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            actividadAlumno.setFechaFin(LocalDateTime.now());
            return;
        }

        // Validate responses
        if (respuestasIds == null || respuestasIds.size() != totalPreguntas) {
            throw new IllegalArgumentException("El número de respuestas no coincide con el número de preguntas");
        }

        Set<Long> respuestasUnicas = new HashSet<>(respuestasIds);
        if (respuestasUnicas.size() != respuestasIds.size()) {
            throw new IllegalArgumentException("Hay respuestas duplicadas");
        }

        Set<Long> preguntasRespondidas = new HashSet<>();

        for (Long respuestaAlumnoId : respuestasIds) {
            RespAlumnoGeneral respAlumno = respAlumnoGeneralService.readRespAlumnoGeneral(respuestaAlumnoId);

            if (!respAlumno.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
                throw new IllegalArgumentException("Una de las respuestas no pertenece a esta actividad del alumno");
            }

            if (!actividadGeneral.getPreguntas().contains(respAlumno.getPregunta())) {
                throw new IllegalArgumentException("Una de las respuestas no pertenece a una pregunta de esta actividad");
            }

            Long preguntaId = respAlumno.getPregunta().getId();
            if (!preguntasRespondidas.add(preguntaId)) {
                throw new IllegalArgumentException("Hay varias respuestas para la misma pregunta");
            }

            respAlumnoGeneralService.corregirRespuestaAlumnoGeneral(respuestaAlumnoId);
        }

        // Time-based scoring: faster completion = more points
        int puntuacionMaxima = actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 0;
        Integer tiempoSegundos = actividadAlumno.getTiempoMinutos() * 60;

        if (tiempoSegundos == null || tiempoSegundos <= 0) {
            // Fallback: full points if time not measured
            actividadAlumno.setPuntuacion(puntuacionMaxima);
            actividadAlumno.setNota(10);
        } else {
            // Ideal time: 5s per pair, max time: 30s per pair (beyond that = minimum score)
            int idealSeconds = totalPreguntas * 5;
            int maxSeconds = totalPreguntas * 30;

            double proporcion;
            if (tiempoSegundos <= idealSeconds) {
                proporcion = 1.0; // perfect score
            } else if (tiempoSegundos >= maxSeconds) {
                proporcion = 0.1; // minimum 10%
            } else {
                // Linear interpolation between ideal and max
                proporcion = 1.0 - 0.9 * ((double)(tiempoSegundos - idealSeconds) / (maxSeconds - idealSeconds));
            }

            int puntuacionFinal = (int) Math.round(proporcion * puntuacionMaxima);
            int notaFinal = (int) Math.round(proporcion * 10);

            actividadAlumno.setPuntuacion(Math.max(puntuacionFinal, 1));
            actividadAlumno.setNota(Math.max(notaFinal, 1));
        }

        actividadAlumno.setFechaFin(LocalDateTime.now());
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteOrdenacion(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        if (respuestasIds == null || respuestasIds.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar una respuesta de ordenacion");
        }
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
        Integer numErrores = numValores - numPosicionesCorrectas;
        puntuacionFinal -= (puntuacionPorRespuesta / 3) * numErrores;
        notaFinal -= (notaPorRespuesta / 3) * numErrores;
        if (puntuacionFinal < 0) {
            puntuacionFinal = 0;
        }
        if (notaFinal < 0) {
            notaFinal = 0;
        }
        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteMarcarImagen(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        if (respuestasIds == null || respuestasIds.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar al menos una respuesta de marcar imagen");
        }
        MarcarImagen actividadMarcarImagen = marcarImagenService.obtenerMarcarImagenPorId(actividad.getId());
        Integer puntuacionTotal = actividadMarcarImagen.getPuntuacion();
        Integer notaTotal = 10;
        Integer puntuacionFinal = 0;
        Integer notaFinal = 0;
        Integer numPuntos = actividadMarcarImagen.getPuntosImagen().size();
        Integer puntuacionPorRespuesta = numPuntos > 0 ? puntuacionTotal / numPuntos : 0;
        Integer notaPorRespuesta = numPuntos > 0 ? notaTotal / numPuntos : 0;

        for (Long respuestaId : respuestasIds) {
            RespAlumnoPuntoImagen respuestaAlumno = respAlumnoPuntoImagenService.encontrarRespuestaAlumnoPuntoImagenPorId(respuestaId);
            actividadAlumno.getRespuestasAlumno().add(respuestaAlumno);
            if(respuestaAlumno == null) {
                throw new ResourceNotFoundException("RespAlumnoPuntoImagen", "id", respuestaId);
            } else if (!respuestaAlumno.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
                throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no pertenece a la actividad del alumno con id " + actividadAlumno.getId());
            }
            boolean esCorrecta = respAlumnoPuntoImagenService.corregirRespuestaAlumnoPuntoImagen(respuestaId);
            if (esCorrecta) {
                puntuacionFinal += puntuacionPorRespuesta;
                notaFinal += notaPorRespuesta;
            }
                else {
                    puntuacionFinal -= puntuacionPorRespuesta / 2;
                    notaFinal -= notaPorRespuesta / 2;
                }
        }
        if (puntuacionFinal <0) {
            puntuacionFinal = 0;
        }
        if (notaFinal <0) {
            notaFinal = 0;
        }
        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }

    public void corregirActividadAlumnoAutomaticamenteCrucigrama(ActividadAlumno actividadAlumno, Actividad actividad) {
        // Fetch the General entity fresh to ensure preguntas collection is loaded
        General actividadGeneral = (General) actividadRepository.findById(actividad.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        int totalPalabras = actividadGeneral.getPreguntas().size();
        int puntuacionMaxima = actividadGeneral.getPuntuacion() != null ? actividadGeneral.getPuntuacion() : 0;

        if (totalPalabras == 0 || puntuacionMaxima == 0) {
            actividadAlumno.setPuntuacion(puntuacionMaxima);
            actividadAlumno.setNota(10);
            return;
        }

        // Time-based scoring: ideal 30s per word, max 120s per word
        long tiempoSegundos = 0;
        if (actividadAlumno.getFechaInicio() != null && actividadAlumno.getFechaFin() != null) {
            tiempoSegundos = java.time.Duration.between(actividadAlumno.getFechaInicio(), actividadAlumno.getFechaFin()).toSeconds();
        }

        if (tiempoSegundos <= 0) {
            actividadAlumno.setPuntuacion(puntuacionMaxima);
            actividadAlumno.setNota(10);
        } else {
            int idealSeconds = totalPalabras * 30;
            int maxSeconds = totalPalabras * 120;

            double proporcion;
            if (tiempoSegundos <= idealSeconds) {
                proporcion = 1.0;
            } else if (tiempoSegundos >= maxSeconds) {
                proporcion = 0.1;
            } else {
                proporcion = 1.0 - 0.9 * ((double)(tiempoSegundos - idealSeconds) / (maxSeconds - idealSeconds));
            }

            int puntuacionFinalCrucigrama = (int) Math.round(proporcion * puntuacionMaxima);
            int notaFinalCrucigrama = (int) Math.round(proporcion * 10);

            actividadAlumno.setPuntuacion(Math.max(puntuacionFinalCrucigrama, 1));
            actividadAlumno.setNota(Math.max(notaFinalCrucigrama, 1));
        }
    }

    @Override  
    public ActividadAlumno corregirActividadAlumnoAutomaticamenteGeneralClasificacion(Long actividadAlumnoId, List<Long> respuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actividadAlumnoId).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        validarAccesoAlumnoOMaestroPropietario(actividadAlumno);
    
        General actividadGeneral = (General) actividadRepository.findById(actividadAlumno.getActividad().getId()).orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        int numPreguntasTotales = actividadGeneral.getPreguntas().size();    
        if (numPreguntasTotales == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return actividadAlumnoRepository.save(actividadAlumno);
        }
    

        int puntuacionMaximaActividad = (actividadGeneral.getPuntuacion() != null) ? actividadGeneral.getPuntuacion() : 0;
        double notaMaxima = 10.0;

        int numRespuestasTotales = actividadGeneral.getPreguntas().stream()
            .mapToInt(p -> p.getRespuestasMaestro().size())
            .sum();
        if (numRespuestasTotales == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return actividadAlumnoRepository.save(actividadAlumno);
        }

        double valorPuntoPorPregunta = (double) puntuacionMaximaActividad / numRespuestasTotales;
        double valorNotaPorPregunta = notaMaxima / numRespuestasTotales;

        double puntuacionAcumulada = 0;
        double notaAcumulada = 0;
        
        if (respuestasIds != null) {
            for (Long respuestaId : respuestasIds) {
                
                boolean esCorrecta = respAlumnoGeneralService.corregirRespuestaAlumnoGeneralClasificacion(respuestaId);
                if (esCorrecta) {
                    puntuacionAcumulada += valorPuntoPorPregunta;
                    notaAcumulada += valorNotaPorPregunta;
                }
                else {
                    puntuacionAcumulada -= valorPuntoPorPregunta / 2;
                    notaAcumulada -= valorNotaPorPregunta / 2;
                }
            }
        }
        

        if (puntuacionAcumulada <0) {
            puntuacionAcumulada = 0;
        }
        if (notaAcumulada <0) {
            notaAcumulada = 0;
        }
        actividadAlumno.setPuntuacion((int) Math.round(puntuacionAcumulada));
        actividadAlumno.setNota((int) Math.round(notaAcumulada));
        actividadAlumno.setFechaFin(LocalDateTime.now());

        return actividadAlumnoRepository.save(actividadAlumno);
    }

    private void validarAccesoAlumnoOMaestroPropietario(ActividadAlumno actividadAlumno) {
        Usuario current = usuarioService.findCurrentUser();
        if (current instanceof Alumno) {
            Long alumnoIdActividad = actividadAlumno.getAlumno() == null ? null : actividadAlumno.getAlumno().getId();
            if (alumnoIdActividad == null || !alumnoIdActividad.equals(current.getId())) {
                throw new AccessDeniedException("No puedes acceder a una actividad que no es tuya");
            }
            return;
        }

        if (current instanceof Maestro) {
            Long maestroCursoId = actividadAlumno.getActividad().getTema().getCurso().getMaestro().getId();
            if (maestroCursoId == null || !maestroCursoId.equals(current.getId())) {
                throw new AccessDeniedException("Solo el maestro propietario puede acceder a esta actividad");
            }
            return;
        }

        throw new AccessDeniedException("No tienes permisos para acceder a actividades de alumnos");
    }

    private void validarAccesoMaestroPropietario(ActividadAlumno actividadAlumno) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede corregir manualmente una actividad");
        }

        Long maestroCursoId = actividadAlumno.getActividad().getTema().getCurso().getMaestro().getId();
        if (maestroCursoId == null || !maestroCursoId.equals(current.getId())) {
            throw new AccessDeniedException("Solo el maestro propietario puede corregir esta actividad");
        }
    }
  
}
