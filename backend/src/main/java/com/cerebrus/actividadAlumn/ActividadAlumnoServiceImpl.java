package com.cerebrus.actividadAlumn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionService;
import com.cerebrus.actividad.tablero.Tablero;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.comun.utils.AccesoActividadAlumnoUtils;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
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
    public ActividadAlumno crearActAlumno(Integer puntuacion, LocalDateTime fechaInicio,
        LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Long alumnoId, Long actId) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear una actividad alumno");
        }
        if (alumnoId == null || !alumnoId.equals(current.getId())) {
            throw new AccessDeniedException("No puedes crear una ActividadAlumno para otro alumno");
        }

        Optional<ActividadAlumno> existing = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actId);
        if (existing.isPresent()) {
            ActividadAlumno ultimaInstancia = existing.get();
            if (ultimaInstancia.getEstadoActividad() != EstadoActividad.TERMINADA) {
                return ultimaInstancia;
            }
        }

        Actividad actividad = actividadRepository.findById(actId).orElseThrow(() -> new ResourceNotFoundException("La actividad no existe"));
        if (existing.isPresent() && existing.get().getEstadoActividad() == EstadoActividad.TERMINADA
                && !Boolean.TRUE.equals(actividad.getPermitirReintento())) {
            throw new AccessDeniedException("No se permite el reintento para esta actividad");
        }

        Alumno alumno = alumnoRepository.findById(alumnoId).orElseThrow(() -> new ResourceNotFoundException("El alumno no existe"));
        
        AccesoActividadAlumnoUtils.validarActividadDesbloqueadaParaAlumno(actividad, alumno.getId());

        ActividadAlumno actividadAlumno = new ActividadAlumno(puntuacion, 
            fechaInicio, fechaFin, nota, numAbandonos, alumno, actividad);

        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadAlumno encontrarActAlumnoPorId(Long id) {
        return actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActividadAlumno> encontrarActAlumnoPorAlumnoIdYActId(Long alumnoId, Long actividadId) {
        return actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actividadId);
    }

    @Override
    @Transactional
    public ActividadAlumno actualizarActAlumno(Long id, Integer puntuacion,
         LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer nota, Integer numAbandonos, Boolean solucionUsada) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        actividadAlumno.setPuntuacion(puntuacion);
        actividadAlumno.setFechaInicio(fechaInicio);
        actividadAlumno.setFechaFin(fechaFin);
        actividadAlumno.setNota(nota);
        actividadAlumno.setNumAbandonos(numAbandonos);
        if (solucionUsada != null) {
            actividadAlumno.setSolucionUsada(solucionUsada);
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    @Transactional
    public void eliminarActAlumnoPorId(Long id) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede eliminar una actividad alumno");
        }
        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes eliminar una ActividadAlumno que no es tuya");
        }
        actividadAlumnoRepository.delete(actividadAlumno);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer existeActAlumnoPorActIdYCurrentUserId(Long actividadId) {
        // Devuelve 1 si existe ActividadAlumno para el alumno autenticado y actividadId; si no 0.
        // No debe lanzar error si no existe.
        try {
            Usuario current = usuarioService.findCurrentUser();
            if (!(current instanceof Alumno)) {
                throw new AccessDeniedException("Solo un alumno puede comprobar si una actividad alumno es suya");
            } 
            if (current == null || current.getId() == null) {
                return 0;
            }
            Long alumnoId = current.getId();
            Optional<ActividadAlumno> ultimoIntento = actividadAlumnoRepository.findByAlumnoIdAndActividadId(alumnoId, actividadId);
            if (ultimoIntento.isEmpty()) {
                return 0;
            }

            if (ultimoIntento.get().getEstadoActividad() != EstadoActividad.TERMINADA) {
                return 1;
            }

            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    @Transactional
    public ActividadAlumno abandonarActAlumnoPorId(Long actividadAlumnoId) {
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede abandonar su actividad");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actividadAlumnoId)
            .orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));

        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes abandonar una ActividadAlumno que no es tuya");
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
    public ActividadAlumno corregirActAlumnoManual(Long id, Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        if (nuevaNota != null) {
            corregirNotaActAlumno(actividadAlumno, nuevaNota);
        }
        if (nuevasCorreccionesRespuestasIds != null && !nuevasCorreccionesRespuestasIds.isEmpty()) {
            corregirRespuestasActAlumno(actividadAlumno, nuevasCorreccionesRespuestasIds);
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    @Override
    public void corregirNotaActAlumno(ActividadAlumno actividadAlumno, Integer nuevaNota) {
        actividadAlumno.setNota(nuevaNota);
    }

    @Override
    public void corregirRespuestasActAlumno(ActividadAlumno actividadAlumno, List<Long> nuevasCorreccionesRespuestasIds) {
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
    public ActividadAlumno corregirActAlumnoAutomaticamente(Long id, List<Long> respuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede enviar a ser corregida automáticamente una actividad alumno");
        }
        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes enviar a una corrección automática una Actividad Alumno que no es tuya");
        }
        Actividad actividad = actividadAlumno.getActividad();
        LocalDateTime finalActividad = LocalDateTime.now();
        actividadAlumno.setFechaFin(finalActividad);
        if(actividad instanceof General){
            if (((General) actividad).getTipo() == TipoActGeneral.TEST) {
                corregirActAlumnoAutomaticamenteGeneral(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.CARTA){
                corregirActAlumnoAutomaticamenteTipoCarta(actividadAlumno, respuestasIds, actividad);
            } else if (((General) actividad).getTipo() == TipoActGeneral.TEORIA){
              // CASO PARA TEORÍA: Simplemente marcamos como terminada y damos la puntuación base
               actividadAlumno.setPuntuacion(actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 1);
               actividadAlumno.setNota(10); // Nota máxima por leer
               actividadAlumno.setFechaFin(LocalDateTime.now());
            } else if (((General) actividad).getTipo() == TipoActGeneral.CRUCIGRAMA){
                corregirActAlumnoAutomaticamenteTipoCrucigrama(actividadAlumno, actividad);
            }
        } else if(actividad instanceof Ordenacion) {
            corregirActAlumnoAutomaticamenteTipoOrdenacion(actividadAlumno, respuestasIds, actividad);
        } else if(actividad instanceof MarcarImagen){
            corregirActAlumnoAutomaticamenteTipoMarcarImagen(actividadAlumno, respuestasIds, actividad);
        } else if (actividad instanceof Tablero) {
            corregirActAlumnoAutomaticamenteTipoTablero(actividadAlumno, actividad);
        } else {    
        // CASO PARA TEORÍA: se ha movido dentro de instancia de GENERAL porque la actividad de teoria es una actividad general
        }
        return actividadAlumnoRepository.save(actividadAlumno);
    }

    private void corregirActAlumnoAutomaticamenteGeneral(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        General actividadGeneral = (General) actividad;
        int numPreguntasTotales = actividadGeneral.getPreguntas().size();
        if (numPreguntasTotales == 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return;
        }

        int puntuacionMaximaActividad = (actividad.getPuntuacion() != null) ? actividad.getPuntuacion() : 0;
        double notaMaxima = 10.0;

        double valorPuntoPorPregunta = (double) puntuacionMaximaActividad / numPreguntasTotales;
        double valorNotaPorPregunta = notaMaxima / numPreguntasTotales;

        double puntuacionAcumulada = 0;
        double notaAcumulada = 0;

        Map<Long, List<RespAlumnoGeneral>> respuestasPorPregunta = new HashMap<>();

        if (respuestasIds != null) {
            for (Long respuestaId : respuestasIds) {
                RespuestaAlumno respuestaAlumno = respuestaAlumnoService.encontrarRespuestaAlumnoPorId(respuestaId);
                if (respuestaAlumno == null) {
                    throw new ResourceNotFoundException("RespuestaAlumno", "id", respuestaId);
                }
                if (!(respuestaAlumno instanceof RespAlumnoGeneral respGeneral)) {
                    throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no es de tipo test");
                }
                if (!respGeneral.getActividadAlumno().getId().equals(actividadAlumno.getId())) {
                    throw new IllegalArgumentException(
                        "La respuesta con id " + respuestaId + " no pertenece a la actividad del alumno con id " + actividadAlumno.getId()
                    );
                }
                if (respGeneral.getPregunta() == null || respGeneral.getPregunta().getId() == null) {
                    throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no tiene pregunta asociada");
                }
                if (!actividadGeneral.getPreguntas().stream().anyMatch(p -> p.getId().equals(respGeneral.getPregunta().getId()))) {
                    throw new IllegalArgumentException("La respuesta con id " + respuestaId + " no pertenece a una pregunta de este test");
                }

                respuestasPorPregunta
                    .computeIfAbsent(respGeneral.getPregunta().getId(), ignored -> new ArrayList<>())
                    .add(respGeneral);
            }
        }

        for (Pregunta pregunta : actividadGeneral.getPreguntas()) {
            List<String> correctasEsperadas = pregunta.getRespuestasMaestro().stream()
                .filter(r -> Boolean.TRUE.equals(r.getCorrecta()))
                .map(r -> r.getRespuesta() == null ? "" : r.getRespuesta().strip().toLowerCase())
                .toList();

            List<String> seleccionadasAlumno = respuestasPorPregunta
                .getOrDefault(pregunta.getId(), List.of())
                .stream()
                .map(r -> r.getRespuesta() == null ? "" : r.getRespuesta().strip().toLowerCase())
                .toList();

            Map<String, Long> conteoCorrectasEsperadas = correctasEsperadas.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting()));

            Map<String, Long> conteoSeleccionadasAlumno = seleccionadasAlumno.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting()));

            boolean preguntaCorrecta =
                !seleccionadasAlumno.isEmpty()
                && !correctasEsperadas.isEmpty()
                && seleccionadasAlumno.size() == correctasEsperadas.size()
                && conteoSeleccionadasAlumno.equals(conteoCorrectasEsperadas);

            if (preguntaCorrecta) {
                puntuacionAcumulada += valorPuntoPorPregunta;
                notaAcumulada += valorNotaPorPregunta;
            } else {
                puntuacionAcumulada -= valorPuntoPorPregunta / 2;
                notaAcumulada -= valorNotaPorPregunta / 2;
            }
        }

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

    private void corregirActAlumnoAutomaticamenteTipoCarta(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
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
            RespAlumnoGeneral respAlumno = respAlumnoGeneralService.encontrarRespuestaAlumnoGeneralPorId(respuestaAlumnoId);

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

    private void corregirActAlumnoAutomaticamenteTipoOrdenacion(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        Ordenacion actividadOrdenacion = ordenacionService.encontrarActOrdenacionPorId(actividad.getId());
        int puntuacionTotal = actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 0;
        int notaTotal = 10;
        int numValores = actividadOrdenacion.getValores().size();

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
        int numPosicionesCorrectas = respAlumnoOrdenacionService.obtenerNumPosicionesCorrectas(respuestasIds.get(0));

        if (numValores <= 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return;
        }

        int numErrores = Math.max(0, numValores - numPosicionesCorrectas);
        double proporcionAciertos = (double) numPosicionesCorrectas / numValores;
        double penalizacionErrores = (double) numErrores / (3.0 * numValores);
        double proporcionFinal = Math.max(0.0, proporcionAciertos - penalizacionErrores);

        // Penaliza comprobaciones fallidas previas dentro del mismo intento.
        // Si el alumno falla varias veces y luego acierta, no debe conservar 10/10.
        int numFallosPrevios = actividadAlumno.getNumFallos() == null ? 0 : actividadAlumno.getNumFallos();
        if (numFallosPrevios > 0) {
            double penalizacionIntentos = Math.min(0.9, numFallosPrevios * 0.1);
            proporcionFinal = Math.max(0.0, proporcionFinal * (1.0 - penalizacionIntentos));
        }

        int puntuacionFinal = (int) Math.round(puntuacionTotal * proporcionFinal);
        int notaFinal = (int) Math.round(notaTotal * proporcionFinal);

        actividadAlumno.setPuntuacion(Math.max(puntuacionFinal, 0));
        actividadAlumno.setNota(Math.max(notaFinal, 0));
    }

    private void corregirActAlumnoAutomaticamenteTipoMarcarImagen(ActividadAlumno actividadAlumno, List<Long> respuestasIds, Actividad actividad) {
        MarcarImagen actividadMarcarImagen = marcarImagenService.encontrarActMarcarImagenPorId(actividad.getId());
        int puntuacionTotal = actividadMarcarImagen.getPuntuacion() != null ? actividadMarcarImagen.getPuntuacion() : 0;
        int notaTotal = 10;
        int numPuntos = actividadMarcarImagen.getPuntosImagen().size();
        int numCorrectas = 0;

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
                numCorrectas++;
            }
        }

        if (numPuntos <= 0) {
            actividadAlumno.setPuntuacion(0);
            actividadAlumno.setNota(0);
            return;
        }

        double proporcionCorrectas = (double) numCorrectas / numPuntos;
        int puntuacionFinal = (int) Math.round(puntuacionTotal * proporcionCorrectas);
        int notaFinal = (int) Math.round(notaTotal * proporcionCorrectas);

        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
    }

    private void corregirActAlumnoAutomaticamenteTipoTablero(ActividadAlumno actividadAlumno, Actividad actividad) {
        int puntuacionMaxima = actividad.getPuntuacion() != null ? actividad.getPuntuacion() : 0;
        int numFallosTotales = actividadAlumno.getRespuestasAlumno().stream()
            .filter(RespAlumnoGeneral.class::isInstance)
            .map(RespAlumnoGeneral.class::cast)
            .mapToInt(resp -> java.util.Objects.requireNonNullElse(resp.getNumFallos(), 0))
            .sum();

        int notaFinal = Math.max(0, 10 - numFallosTotales);
        int puntuacionFinal = (int) Math.round(puntuacionMaxima * (notaFinal / 10.0));

        actividadAlumno.setPuntuacion(puntuacionFinal);
        actividadAlumno.setNota(notaFinal);
        actividadAlumno.setFechaFin(LocalDateTime.now());
    }

    private void corregirActAlumnoAutomaticamenteTipoCrucigrama(ActividadAlumno actividadAlumno, Actividad actividad) {
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
        boolean penalizacionPorSolucion = Boolean.TRUE.equals(actividadAlumno.getSolucionUsada());
        long tiempoSegundos = 0;
        if (actividadAlumno.getFechaInicio() != null && actividadAlumno.getFechaFin() != null) {
            tiempoSegundos = java.time.Duration.between(actividadAlumno.getFechaInicio(), actividadAlumno.getFechaFin()).toSeconds();
        }

        if (penalizacionPorSolucion) {
            actividadAlumno.setPuntuacion(Math.max((int) Math.round(puntuacionMaxima * 0.1), 1));
            actividadAlumno.setNota(1);
        } else if (tiempoSegundos <= 0) {
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
    @Transactional  
    public ActividadAlumno corregirActAlumnoAutomaticamenteClasificacion(Long actividadAlumnoId, List<Long> respuestasIds) {
        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actividadAlumnoId).orElseThrow(() -> new ResourceNotFoundException("La actividad del alumno no existe"));
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede enviar a ser corregida automáticamente una actividad alumno de clasificación");
        }
        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes enviar a una corrección automática una Actividad Alumno de clasificación que no es tuya");
        }
    
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
            }
        }

        actividadAlumno.setPuntuacion((int) Math.round(puntuacionAcumulada));
        actividadAlumno.setNota((int) Math.round(notaAcumulada));
        actividadAlumno.setFechaFin(LocalDateTime.now());

        return actividadAlumnoRepository.save(actividadAlumno);
    }
  
}
