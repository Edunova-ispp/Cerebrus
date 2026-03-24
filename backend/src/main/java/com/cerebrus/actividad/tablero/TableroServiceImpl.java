package com.cerebrus.actividad.tablero;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.comun.enumerados.TamanoTablero;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.actividad.tablero.dto.TableroRequest;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.actividadAlumn.ActividadAlumnoService;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralRepository;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class TableroServiceImpl implements TableroService {

    private final TableroRepository tableroRepository;
    private final ActividadRepository actividadRepository;
    private final TemaRepository temaRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaMaestroRepository respuestaMaestroRepository;
    private final UsuarioService usuarioService;
    private final ActividadAlumnoService actividadAlumnoService;
    private final RespAlumnoGeneralRepository respuestaAlumnoRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;

    @Autowired
    public TableroServiceImpl(TableroRepository tableroRepository, ActividadRepository actividadRepository, TemaRepository temaRepository, PreguntaRepository preguntaRepository, RespuestaMaestroRepository respuestaRepository, UsuarioService usuarioService, ActividadAlumnoService actividadAlumnoService, RespAlumnoGeneralRepository respuestaAlumnoRepository, ActividadAlumnoRepository actividadAlumnoRepository) {
        this.actividadRepository = actividadRepository;
        this.tableroRepository = tableroRepository;
        this.temaRepository = temaRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaMaestroRepository = respuestaRepository;
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
        
        Tema tema = temaRepository.findById(actividad.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado"));
                
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

        for (Map.Entry<String, String> preguntaRespuesta : actividad.getPreguntasYRespuestas().entrySet()) {
            
            Pregunta pregunta = new Pregunta();
            pregunta.setPregunta(preguntaRespuesta.getKey());
            pregunta.setActividad(tablero); 

            RespuestaMaestro respuesta = new RespuestaMaestro();
            respuesta.setRespuesta(preguntaRespuesta.getValue());
            respuesta.setCorrecta(true);
            respuesta.setPregunta(pregunta); 

          
            pregunta.getRespuestasMaestro().add(respuesta);
            tablero.getPreguntas().add(pregunta);
        }

        Tablero tableroCreado = tableroRepository.save(tablero);

        return TableroDTO.fromEntity(tableroCreado);
    }

    @Override
    @Transactional(readOnly = true)
    public TableroDTO getTablero(Long tableroId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Usuario current = usuarioService.findCurrentUser();
        if (current instanceof Maestro) {
            Maestro maestro = (Maestro) current;
            if (!tablero.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para acceder a este tablero");
            }
            return TableroDTO.fromEntity(tablero);
        }
        List<Inscripcion> inscripciones = tablero.getTema().getCurso().getInscripciones();
        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion.getAlumno().getId().equals(current.getId())) {
                return TableroDTO.fromEntity(tablero); 
            }
        }
        // Alumnos can access tablero data to play the activity
        throw new AccessDeniedException("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    @Override
    @Transactional
    public void eliminarTablero(Long tableroId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new ResourceNotFoundException("Tablero no encontrado"));
        Usuario u = usuarioService.findCurrentUser();
        if (u instanceof Maestro) {
            Maestro maestro = (Maestro) u;
            if (!tablero.getTema().getCurso().getMaestro().getId().equals(maestro.getId())) {
                throw new AccessDeniedException("No tienes permiso para eliminar este tablero porque no eres el maestro del curso");
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
                throw new AccessDeniedException("No tienes permiso para actualizar este tablero porque no eres el maestro del curso");
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
            respuestaMaestroRepository.deleteAll(pregunta.getRespuestasMaestro());
            preguntaRepository.delete(pregunta);
        }
        tableroExistente.getPreguntas().clear();

        for (Map.Entry<String, String> preguntaRespuesta : tablero.getPreguntasYRespuestas().entrySet()) {
            Pregunta pregunta = new Pregunta();
            pregunta.setPregunta(preguntaRespuesta.getKey());
            pregunta.setActividad(tableroExistente); 

            RespuestaMaestro respuesta = new RespuestaMaestro();
            respuesta.setRespuesta(preguntaRespuesta.getValue());
            respuesta.setCorrecta(true);
            respuesta.setPregunta(pregunta); 

            pregunta.getRespuestasMaestro().add(respuesta);
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
            
            Boolean correcta = pregunta.getRespuestasMaestro().get(0).getRespuesta().toLowerCase().strip().equals(cleanedRespuesta.toLowerCase().strip());
            
            ActividadAlumno actividadAlumno = actividadAlumnoService.crearActividadAlumno(0, LocalDateTime.now(), null, 0, 0, alumno.getId(), tablero.getId());
            
            RespAlumnoGeneral respuestaAlumno = new RespAlumnoGeneral(correcta, actividadAlumno, respuesta, pregunta);
            actividadAlumno.getRespuestasAlumno().add(respuestaAlumno);
            
            if(correcta  && tablero.getPreguntas().get(tablero.getPreguntas().size()-1).getId().equals(pregunta.getId())) {
                actividadAlumno.setFechaFin(LocalDateTime.now());
                int numErrores = 0;
                for(RespuestaAlumno resp : actividadAlumno.getRespuestasAlumno()) {
                    if(!resp.getCorrecta()) {
                        numErrores++;
                    }
                }
                Integer notaFinal = Math.round(10 - (numErrores * (10 / tablero.getPreguntas().size()/4)));
                if (notaFinal <= 0) {
                    notaFinal = 1;
                }
                Integer puntuacionFinal = Math.round(tablero.getPuntuacion() - (numErrores * (tablero.getPuntuacion() / tablero.getPreguntas().size()/4)));
                if (puntuacionFinal <= 0) {
                    puntuacionFinal = 1;
                }
                actividadAlumno.setNota(notaFinal);
                actividadAlumno.setPuntuacion(puntuacionFinal);
            }
            
            actividadAlumnoRepository.save(actividadAlumno);
            
            if(pregunta.getActividad().getRespVisible()) {
                return correcta ? "Respuesta correcta" : "Respuesta incorrecta. La respuesta correcta es: " + pregunta.getRespuestasMaestro().get(0).getRespuesta();
            } else {
                return correcta ? "Respuesta correcta" : "Respuesta incorrecta";
            }

        } else {
            throw new AccessDeniedException("Solo un alumno puede responder a actividades de tablero");
        }
    }
}
