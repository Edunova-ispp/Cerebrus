package com.cerebrus.respuestaAlumn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.tablero.Tablero;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaAlumn.dto.PreguntaRespuestaDTO;
import com.cerebrus.respuestaAlumn.dto.RespuestasActividadDTO;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacion;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;


@Service
@Transactional
public class RespuestaAlumnoServiceImpl implements RespuestaAlumnoService {

    private final RespuestaAlumnoRepository respuestaAlumnoRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public RespuestaAlumnoServiceImpl(
        RespuestaAlumnoRepository respuestaAlumnoRepository,
        ActividadAlumnoRepository actividadAlumnoRepository,
        UsuarioService usuarioService
    ) {
        this.respuestaAlumnoRepository = respuestaAlumnoRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id) {
        return respuestaAlumnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumno", "id", id));
    }

    @Override
    public RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id) {
        RespuestaAlumno respuestaAlumno = respuestaAlumnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RespuestaAlumno", "id", id));

        Boolean esCorrecta = respuestaAlumno.getCorrecta();
        if (esCorrecta == null) {
            esCorrecta = true; // Si es null, lo consideramos como no marcada, así que la marcamos como correcta
        } else {
            esCorrecta = !esCorrecta;
        }

        respuestaAlumno.setCorrecta(esCorrecta);
        return respuestaAlumnoRepository.save(respuestaAlumno);
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestasActividadDTO obtenerRespuestasActividadAlumno(Long actividadId) {
        Usuario usuario = usuarioService.findCurrentUser();
        if (!(usuario instanceof Alumno)) {
            throw new AccessDeniedException("Solo los alumnos pueden acceder a sus respuestas.");
        }
        
        Alumno alumnoActual = (Alumno) usuario;

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findByAlumnoIdAndActividadId(
            alumnoActual.getId(), actividadId
        ).orElseThrow(() -> new ResourceNotFoundException("Parece que el alumno no ha participado en esta actividad o la actividad no existe.")
        );

        if (actividadAlumno.getEstadoActividad() != EstadoActividad.TERMINADA) {
            throw new AccessDeniedException("La actividad no ha sido completada. No se pueden consultar las respuestas.");
        }

        if (!alumnoActual.getOrganizacion().getId()
                .equals(actividadAlumno.getActividad().getTema().getCurso().getMaestro().getOrganizacion().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver las respuestas de esta actividad.");
        }

        Actividad actividad = actividadAlumno.getActividad();
        Boolean mostrarRespuestasAlumno = actividad.getEncontrarRespuestaAlumno();
        Boolean mostrarRespuestasMaestro = actividad.getEncontrarRespuestaMaestro();

        RespuestasActividadDTO dto = new RespuestasActividadDTO();
        dto.setActividadId(actividad.getId());
        dto.setActividadTitulo(actividad.getTitulo());
        dto.setNota(actividadAlumno.getNota());

        List<RespuestaAlumno> respuestas = actividadAlumno.getRespuestasAlumno();
        Boolean todasCorrectas = respuestas.stream()
            .allMatch(r -> r.getCorrecta() != null && r.getCorrecta());
        dto.setCompletadaCorrectamente(todasCorrectas);

        List<PreguntaRespuestaDTO> preguntas = construirPreguntasRespuestas(
            actividad, actividadAlumno, mostrarRespuestasAlumno, mostrarRespuestasMaestro
        );
        dto.setPreguntas(preguntas);

        return dto;
    }

    
    private List<PreguntaRespuestaDTO> construirPreguntasRespuestas(
        Actividad actividad,
        ActividadAlumno actividadAlumno,
        Boolean mostrarRespuestasAlumno,
        Boolean mostrarRespuestasMaestro
    ) {
        List<PreguntaRespuestaDTO> preguntas = new ArrayList<>();

        List<RespuestaAlumno> respuestasAlumno = actividadAlumno.getRespuestasAlumno();

        for (RespuestaAlumno respuesta : respuestasAlumno) {
            PreguntaRespuestaDTO pDto = new PreguntaRespuestaDTO();

            if (respuesta instanceof RespAlumnoGeneral) {
                if (actividad instanceof Tablero) {
                    procesarRespuestaTablero(
                        (RespAlumnoGeneral) respuesta, pDto,
                        mostrarRespuestasAlumno, mostrarRespuestasMaestro
                    );
                } else {
                    procesarRespuestaGeneral(
                        (RespAlumnoGeneral) respuesta, pDto, 
                        mostrarRespuestasAlumno, mostrarRespuestasMaestro
                    );
                }
            } else if (respuesta instanceof RespAlumnoOrdenacion) {
                procesarRespuestaOrdenacion(
                    (RespAlumnoOrdenacion) respuesta, pDto,
                    mostrarRespuestasAlumno, mostrarRespuestasMaestro
                );
            } else if (respuesta instanceof RespAlumnoPuntoImagen) {
                procesarRespuestaPuntoImagen(
                    (RespAlumnoPuntoImagen) respuesta, pDto,
                    mostrarRespuestasAlumno, mostrarRespuestasMaestro
                );
            }

            if (pDto.getPreguntaId() != null) {
                pDto.setRespuestaAlumnoCorrecta(respuesta.getCorrecta());
                preguntas.add(pDto);
            }
        }

        return preguntas;
    }

    
    private void procesarRespuestaGeneral(
        RespAlumnoGeneral respuesta,
        PreguntaRespuestaDTO pDto,
        Boolean mostrarRespuestasAlumno,
        Boolean mostrarRespuestasMaestro
    ) {
        com.cerebrus.pregunta.Pregunta pregunta = respuesta.getPregunta();
        pDto.setPreguntaId(pregunta.getId());
        pDto.setPregunta(pregunta.getPregunta());
        pDto.setImagenPregunta(pregunta.getImagen());

        if (mostrarRespuestasAlumno && respuesta.getRespuesta() != null) {
            pDto.setRespuestaAlumno(respuesta.getRespuesta());
        } else {
            pDto.setRespuestaAlumno(null);
        }

        if (mostrarRespuestasMaestro && pregunta.getRespuestasMaestro() != null) {
            String respuestaCorrecta = pregunta.getRespuestasMaestro().stream()
                .filter(r -> r.getCorrecta() != null && r.getCorrecta())
                .map(r -> r.getRespuesta())
                .collect(Collectors.joining("; "));
            pDto.setRespuestaCorrecta(respuestaCorrecta.isEmpty() ? null : respuestaCorrecta);
        } else {
            pDto.setRespuestaCorrecta(null);
        }
    }

    
    private void procesarRespuestaOrdenacion(
        RespAlumnoOrdenacion respuesta,
        PreguntaRespuestaDTO pDto,
        Boolean mostrarRespuestasAlumno,
        Boolean mostrarRespuestasMaestro
    ) {
        com.cerebrus.actividad.ordenacion.Ordenacion ordenacion = respuesta.getOrdenacion();
        pDto.setPreguntaId(ordenacion.getId());
        pDto.setPregunta("Ordenación: " + ordenacion.getTitulo());
        pDto.setImagenPregunta(null);

        if (mostrarRespuestasAlumno && respuesta.getValoresAlum() != null) {
            pDto.setRespuestaAlumno(respuesta.getValoresAlum());
        } else {
            pDto.setRespuestaAlumno(null);
        }

        if (mostrarRespuestasMaestro && ordenacion.getValores() != null) {
            pDto.setRespuestaCorrecta(ordenacion.getValores());
        } else {
            pDto.setRespuestaCorrecta(null);
        }
    }


    private void procesarRespuestaTablero(
        RespAlumnoGeneral respuesta,
        PreguntaRespuestaDTO pDto,
        Boolean mostrarRespuestasAlumno,
        Boolean mostrarRespuestasMaestro
    ) {
        com.cerebrus.pregunta.Pregunta pregunta = respuesta.getPregunta();
        pDto.setPreguntaId(pregunta.getId());
        pDto.setPregunta(pregunta.getPregunta());
        pDto.setImagenPregunta(pregunta.getImagen());

        
        if (mostrarRespuestasAlumno && respuesta.getRespuesta() != null) {
            pDto.setRespuestaAlumno(respuesta.getRespuesta());
        } else {
            pDto.setRespuestaAlumno(null);
        }

        if (mostrarRespuestasMaestro && pregunta.getRespuestasMaestro() != null) {
            String respuestaCorrecta = pregunta.getRespuestasMaestro().stream()
                .filter(r -> r.getCorrecta() != null && r.getCorrecta())
                .map(r -> r.getRespuesta())
                .collect(Collectors.joining("; "));
            pDto.setRespuestaCorrecta(respuestaCorrecta.isEmpty() ? null : respuestaCorrecta);
        } else {
            pDto.setRespuestaCorrecta(null);
        }
    }

    
    private void procesarRespuestaPuntoImagen(
        RespAlumnoPuntoImagen respuesta,
        PreguntaRespuestaDTO pDto,
        Boolean mostrarRespuestasAlumno,
        Boolean mostrarRespuestasMaestro
    ) {
        com.cerebrus.puntoImagen.PuntoImagen puntoImagen = respuesta.getPuntoImagen();
        pDto.setPreguntaId(puntoImagen.getId());
        pDto.setPregunta("Selecciona el punto: " + puntoImagen.getRespuesta());
        pDto.setImagenPregunta(puntoImagen.getMarcarImagen().getImagenAMarcar());

    
        if (mostrarRespuestasAlumno && respuesta.getRespuesta() != null) {
            pDto.setRespuestaAlumno(respuesta.getRespuesta());
        } else {
            pDto.setRespuestaAlumno(null);
        }

    
        if (mostrarRespuestasMaestro && puntoImagen.getRespuesta() != null) {
            pDto.setRespuestaCorrecta(puntoImagen.getRespuesta());
        } else {
            pDto.setRespuestaCorrecta(null);
        }
    }

    
}
