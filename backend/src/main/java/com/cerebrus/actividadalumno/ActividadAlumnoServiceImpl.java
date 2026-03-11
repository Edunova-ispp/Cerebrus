package com.cerebrus.actividadalumno;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.General;
import com.cerebrus.actividad.MarcarImagen;
import com.cerebrus.actividad.MarcarImagenService;
import com.cerebrus.actividad.Ordenacion;
import com.cerebrus.actividad.OrdenacionService;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaalumno.RespAlumnoGeneral;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralService;
import com.cerebrus.respuestaalumno.RespAlumnoOrdenacionService;
import com.cerebrus.respuestaalumno.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaalumno.RespAlumnoPuntoImagenService;
import com.cerebrus.respuestaalumno.RespuestaAlumno;
import com.cerebrus.respuestaalumno.RespuestaAlumnoService;
import com.cerebrus.usuario.AlumnoRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

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

    @Autowired
    public ActividadAlumnoServiceImpl(ActividadAlumnoRepository actividadAlumnoRepository, 
        ActividadRepository actividadRepository, AlumnoRepository alumnoRepository, RespuestaAlumnoService respuestaAlumnoService,
        RespAlumnoGeneralService respAlumnoGeneralService, OrdenacionService ordenacionService, 
        RespAlumnoOrdenacionService respAlumnoOrdenacionService, UsuarioService usuarioService, 
        MarcarImagenService marcarImagenService, RespAlumnoPuntoImagenService respAlumnoPuntoImagenService) {
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
        LocalDateTime finalActividad = LocalDateTime.now();
        LocalDateTime inicioActividad = actividadAlumno.getInicio() != null? actividadAlumno.getInicio() : finalActividad;
        actividadAlumno.setAcabada(finalActividad);
        Integer tiempoSegundos = (int) Duration.between(inicioActividad, finalActividad).getSeconds();
        actividadAlumno.setTiempo(tiempoSegundos);
        if(actividad instanceof General){
            if (((General) actividad).getTipo() == TipoActGeneral.TEST) {
                corregirActividadAlumnoAutomaticamenteGeneral(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.CARTA){
                corregirActividadAlumnoAutomaticamenteCartaGeneral(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.TEORIA){
              // CASO PARA TEORÍA: Simplemente marcamos como terminada y damos la puntuación base
               actividadAlumno.setPuntuacion(actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 1);
               actividadAlumno.setNota(10); // Nota máxima por leer
               actividadAlumno.setAcabada(LocalDateTime.now());
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
            }
        }

    // 3. Guardar y redondear
    actividadAlumno.setPuntuacion((int) Math.round(puntuacionAcumulada));
    actividadAlumno.setNota((int) Math.round(notaAcumulada));
    actividadAlumno.setAcabada(LocalDateTime.now());
    }

    @Override
    public void corregirActividadAlumnoAutomaticamenteCartaGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        General actividadGeneral = (General) actividad;
        int totalPreguntas = actividadGeneral.getPreguntas().size();

        if (totalPreguntas == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            actividadAlumno.setAcabada(LocalDateTime.now());
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
        Integer tiempoSegundos = actividadAlumno.getTiempo();

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

        actividadAlumno.setAcabada(LocalDateTime.now());
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

    @Override
    public void corregirActividadAlumnoAutomaticamenteMarcarImagen(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
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
        }
        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }

    @Override  
    public ActividadAlumno corregirActividadAlumnoAutomaticamenteGeneralClasificacion(Long actividadAlumnoId, List<Long> respuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actividadAlumnoId).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
    
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
            .mapToInt(p -> p.getRespuestas().size())
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
                
                boolean esCorrecta = respAlumnoGeneralService.corregirRespuestaAlumnoGeneralTest(respuestaId);
                System.out.println("Respuesta ID " + respuestaId + " es correcta? " + esCorrecta);
                if (esCorrecta) {
                    puntuacionAcumulada += valorPuntoPorPregunta;
                    notaAcumulada += valorNotaPorPregunta;
                }
            }
        }
        

        
        actividadAlumno.setPuntuacion((int) Math.round(puntuacionAcumulada));
        actividadAlumno.setNota((int) Math.round(notaAcumulada));
        actividadAlumno.setAcabada(LocalDateTime.now());

        return actividadAlumnoRepository.save(actividadAlumno);
    }
  
}
