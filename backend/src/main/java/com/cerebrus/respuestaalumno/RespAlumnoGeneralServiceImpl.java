package com.cerebrus.respuestaalumno;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaRepository;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;


@Service
@Transactional
public class RespAlumnoGeneralServiceImpl implements RespAlumnoGeneralService {

    private final RespAlumnoGeneralRepository respAlumnoGeneralRepository;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public RespAlumnoGeneralServiceImpl(RespAlumnoGeneralRepository respAlumnoGeneralRepository, 
        ActividadAlumnoRepository actividadAlumnoRepository, PreguntaRepository preguntaRepository, 
        RespuestaRepository respuestaRepository, UsuarioService usuarioService) {
        this.respAlumnoGeneralRepository = respAlumnoGeneralRepository;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public RespAlumnoGeneralCreateResponse crearRespAlumnoGeneral(Long actAlumnoId, String respuesta, Long preguntaId) {

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas de alumno");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actAlumnoId).orElseThrow(() -> new RuntimeException("La actividad del alumno no existe"));
        Pregunta pregunta = preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe"));
        Respuesta respuestaObj = respuestaRepository.findByRespuesta(respuesta).orElseThrow(() -> new RuntimeException("La respuesta no existe"));
        Boolean correcta = respuestaObj.getCorrecta();
        String comentariosRespVisible = null;
        if(pregunta.getActividad().getRespVisible()) {
            comentariosRespVisible = pregunta.getActividad().getComentariosRespVisible();
        } else {
            comentariosRespVisible = "";
        }

        RespAlumnoGeneral respAlumnoGeneral = new RespAlumnoGeneral(correcta, actividadAlumno, respuesta, pregunta);
        RespAlumnoGeneral respAlumnoGeneralGuardada = respAlumnoGeneralRepository.save(respAlumnoGeneral);
        return new RespAlumnoGeneralCreateResponse(respAlumnoGeneralGuardada, comentariosRespVisible);
    }

    @Override
    @Transactional(readOnly = true)
    public RespAlumnoGeneral readRespAlumnoGeneral(Long id) {
        return respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
    }

// ESTOS MÉTODOS QUEDAN DEFINIDOS POR SI ES NECESARIO UTILIZARLOS, 
// PERO PARA LA FEATURE 35 NO SON NECESARIOS (POR ELLO POR AHORA NO HAY VALIDACIÓN DE ROLES)

    @Override
    @Transactional
    public RespAlumnoGeneral updateRespAlumnoGeneral(Long id,Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        respAlumnoGeneral.setCorrecta(correcta);
        respAlumnoGeneral.setRespuesta(respuesta);
        respAlumnoGeneral.setPregunta(preguntaRepository.findById(preguntaId).orElseThrow(() -> new RuntimeException("La pregunta no existe")));
        return respAlumnoGeneralRepository.save(respAlumnoGeneral);
    }

    @Override
    @Transactional
    public void deleteRespAlumnoGeneral(Long id) {
        RespAlumnoGeneral respAlumnoGeneral = respAlumnoGeneralRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno no existe"));
        respAlumnoGeneralRepository.delete(respAlumnoGeneral);
    }
}
