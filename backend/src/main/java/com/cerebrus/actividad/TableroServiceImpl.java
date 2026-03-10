package com.cerebrus.actividad;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.TamanoTablero;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.actividadalumno.ActividadAlumnoService;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaRepository;
import com.cerebrus.respuestaalumno.RespAlumnoGeneral;
import com.cerebrus.respuestaalumno.RespAlumnoGeneralRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;


@Service
@Transactional
public class TableroServiceImpl implements TableroService {

    private final TableroRepository tableroRepository;
    private final ActividadRepository actividadRepository;
    private final TemaRepository temaRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final UsuarioService usuarioService;
    private final ActividadAlumnoService actividadAlumnoService;
    private final RespAlumnoGeneralRepository respuestaAlumnoRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;

    @Autowired
    public TableroServiceImpl(TableroRepository tableroRepository, ActividadRepository actividadRepository, TemaRepository temaRepository, PreguntaRepository preguntaRepository, RespuestaRepository respuestaRepository, UsuarioService usuarioService, ActividadAlumnoService actividadAlumnoService, RespAlumnoGeneralRepository respuestaAlumnoRepository, ActividadAlumnoRepository actividadAlumnoRepository) {
        this.actividadRepository = actividadRepository;
        this.tableroRepository = tableroRepository;
        this.temaRepository = temaRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.usuarioService = usuarioService;
        this.actividadAlumnoService = actividadAlumnoService;
        this.respuestaAlumnoRepository = respuestaAlumnoRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
    }

    @Override
    @Transactional
    public TableroDTO crearActividadTablero(TableroRequest actividad)  {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede crear actividades de tablero");
        }
        Tema tema = temaRepository.findById(actividad.getTemaId()).orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado"));
        Maestro maestro = (Maestro) u;
        if (!tema.getCurso().getMaestro().getId().equals(maestro.getId())) {
            throw new AccessDeniedException("No tienes permiso para crear un tablero en este tema");
        }
        Tablero tablero = new Tablero(
            actividad.getTitulo(),
            actividad.getDescripcion(),
            actividad.getPuntuacion(),
            null,
            actividad.getRespVisible(),
            actividadRepository.findMaxPosicionByTemaId(actividad.getTemaId()) + 1,
            1,
            tema,
            actividad.getTamano() ? TamanoTablero.TRES_X_TRES : TamanoTablero.CUATRO_X_CUATRO
        );

        Tablero tableroCreado = tableroRepository.save(tablero);

        for (Map.Entry<String, String> preguntaRespuesta : actividad.getPreguntasYRespuestas().entrySet()) {
            Pregunta pregunta = new Pregunta(preguntaRespuesta.getKey(), null, tableroCreado);
            pregunta = preguntaRepository.save(pregunta);
            Respuesta respuesta = new Respuesta(preguntaRespuesta.getValue(), null, true, pregunta);
            respuesta = respuestaRepository.save(respuesta);
            pregunta.getRespuestas().add(respuesta);
            pregunta = preguntaRepository.save(pregunta);
            tableroCreado.getPreguntas().add(pregunta);
            tableroCreado = tableroRepository.save(tableroCreado);
        }

        return TableroDTO.fromEntity(tableroCreado);
    }

    @Override
    @Transactional(readOnly = true)
    public TableroDTO getTablero(Long tableroId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Usuario u = usuarioService.findCurrentUser();
        if (u instanceof Maestro) {
            Maestro maestro = (Maestro) u;
            if (!tablero.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para acceder a este tablero");
            }
        }
        // Alumnos can access tablero data to play the activity
        return TableroDTO.fromEntity(tablero);
    }

    @Override
    @Transactional
    public void eliminarTablero(Long tableroId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Usuario u = usuarioService.findCurrentUser();
        if (u instanceof Maestro) {
            Maestro maestro = (Maestro) u;
            if (!tablero.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para eliminar este tablero");
            }
        } else {
            throw new AccessDeniedException("Solo un maestro puede eliminar actividades de tablero");
        }
        tableroRepository.delete(tablero);
    }

    @Override
    @Transactional
    public TableroDTO actualizarTablero(Long id, TableroRequest tablero) {
        Tablero tableroExistente = tableroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Usuario u = usuarioService.findCurrentUser();
        if (u instanceof Maestro) {
            Maestro maestro = (Maestro) u;
            if (!tableroExistente.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para actualizar este tablero");
            }
        } else {
            throw new AccessDeniedException("Solo un maestro puede actualizar actividades de tablero");
        }

        tableroExistente.setTitulo(tablero.getTitulo());
        tableroExistente.setDescripcion(tablero.getDescripcion());
        tableroExistente.setPuntuacion(tablero.getPuntuacion());
        tableroExistente.setRespVisible(tablero.getRespVisible());
        tableroExistente.setTamano(tablero.getTamano() ? TamanoTablero.TRES_X_TRES : TamanoTablero.CUATRO_X_CUATRO);
        tableroExistente.setVersion(tableroExistente.getVersion()+1);

        for (Pregunta pregunta : tableroExistente.getPreguntas()) {
            respuestaRepository.deleteAll(pregunta.getRespuestas());
            preguntaRepository.delete(pregunta);
        }
        tableroExistente.getPreguntas().clear();

        for (Map.Entry<String, String> preguntaRespuesta : tablero.getPreguntasYRespuestas().entrySet()) {
            Pregunta pregunta = new Pregunta(preguntaRespuesta.getKey(), null, tableroExistente);
            pregunta = preguntaRepository.save(pregunta);
            Respuesta respuesta = new Respuesta(preguntaRespuesta.getValue(), null, true, pregunta);
            respuesta = respuestaRepository.save(respuesta);
            pregunta.getRespuestas().add(respuesta);
            pregunta = preguntaRepository.save(pregunta);
            tableroExistente.getPreguntas().add(pregunta);
        }

        Tablero tableroActualizado = tableroRepository.save(tableroExistente);
        return TableroDTO.fromEntity(tableroActualizado);
    }

    @Override
    @Transactional
    public String crearRespuestaAPreguntaTablero(String respuesta, Long tableroId, Long preguntaId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
        Usuario u = usuarioService.findCurrentUser();

        if (u instanceof Alumno) {
            Alumno alumno = (Alumno) u;
            if (!tablero.getTema().getCurso().getInscripciones().stream().anyMatch(i -> i.getAlumno().getId().equals(alumno.getId()))) {
                throw new AccessDeniedException("No tienes permiso para responder a este tablero porque no esta inscrito");
            }
            if(!tablero.getPreguntas().stream().anyMatch(p -> p.getId().equals(preguntaId))) {
                throw new AccessDeniedException("La pregunta no pertenece a este tablero");
            }
            
            String cleanedRespuesta = respuesta.strip();
            if (cleanedRespuesta.startsWith("\"") && cleanedRespuesta.endsWith("\"")) {
                cleanedRespuesta = cleanedRespuesta.substring(1, cleanedRespuesta.length() - 1);
            }
            Boolean correcta = pregunta.getRespuestas().get(0).getRespuesta().toLowerCase().strip().equals(cleanedRespuesta.toLowerCase().strip());
            ActividadAlumno actividadAlumno = actividadAlumnoService.crearActividadAlumno(0, 0, LocalDateTime.now(), null, 0, 0, alumno.getId(), tablero.getId());
            RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral(correcta, actividadAlumno, respuesta, pregunta);
            respuestaAlumno =  respuestaAlumnoRepository.save(respuestaAlumno);
            actividadAlumno.getRespuestasAlumno().add(respuestaAlumno);
            actividadAlumno = actividadAlumnoRepository.save(actividadAlumno);
            if(correcta  && tablero.getPreguntas().get(tablero.getPreguntas().size()-1).getId().equals(pregunta.getId())) {
                actividadAlumno.setAcabada(LocalDateTime.now());
                actividadAlumno.setNota(10);
                actividadAlumno.setPuntuacion(tablero.getPuntuacion());
                actividadAlumnoRepository.save(actividadAlumno);
            }
            if(pregunta.getActividad().getRespVisible()) {
                return correcta ? "Respuesta correcta" : "Respuesta incorrecta. La respuesta correcta es: " + pregunta.getRespuestas().get(0).getRespuesta();
            } else {
                return correcta ? "Respuesta correcta" : "Respuesta incorrecta";
            }

        } else {
            throw new AccessDeniedException("Solo un alumno puede responder a actividades de tablero");
        }
        
    }
}
