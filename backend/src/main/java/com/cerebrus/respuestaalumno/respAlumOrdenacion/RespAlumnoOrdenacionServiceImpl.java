package com.cerebrus.respuestaAlumno.respAlumOrdenacion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividad.ordenacion.OrdenacionRepository;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.respuestaAlumno.respAlumOrdenacion.dto.RespAlumnoOrdenacionDTO;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;


@Service
@Transactional
public class RespAlumnoOrdenacionServiceImpl implements RespAlumnoOrdenacionService {

    private final RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository;
    private final UsuarioService usuarioService;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final OrdenacionRepository ordenacionRepository;
    private final RespuestaMaestroService respuestaService;

    @Autowired
    public RespAlumnoOrdenacionServiceImpl(RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository, RespuestaMaestroService respuestaService, UsuarioService usuarioService,
         ActividadAlumnoRepository actividadAlumnoRepository, OrdenacionRepository ordenacionRepository) {
        this.respAlumnoOrdenacionRepository = respAlumnoOrdenacionRepository;
        this.usuarioService = usuarioService;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.ordenacionRepository = ordenacionRepository;
        this.respuestaService = respuestaService;
    }

    @Override
    @Transactional
    public RespAlumnoOrdenacionCreateResponse crearRespAlumnoOrdenacion(Long actAlumnoId, List<String> valoresAlum, Long actOrdId) {
        
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas de alumno a actividades de ordenación");
        }

        ActividadAlumno actividadAlumno = actividadAlumnoRepository.findById(actAlumnoId).orElseThrow(() -> new RuntimeException("La actividad del alumno no existe"));
        Ordenacion ordenacion = ordenacionRepository.findById(actOrdId).orElseThrow(() -> new RuntimeException("La actividad de ordenación no existe"));

        List<String> valoresCorrectos = ordenacion.getValores();
        Boolean correcta = valoresAlum.equals(valoresCorrectos);
        Integer numFallosAntes = actividadAlumno.getNumFallos();
        String comentario = null;
        if (ordenacion.getRespVisible()) {
            comentario = ordenacion.getComentariosRespVisible();
        } else {
            comentario = "";
        }

        RespAlumnoOrdenacion respAlumnoOrdenacion = new RespAlumnoOrdenacion();
        respAlumnoOrdenacion.setActividadAlumno(actividadAlumno);
        respAlumnoOrdenacion.setOrdenacion(ordenacion);
        respAlumnoOrdenacion.setValoresAlum(valoresAlum);
        respAlumnoOrdenacion.setCorrecta(correcta);
        respAlumnoOrdenacionRepository.save(respAlumnoOrdenacion);

        // Si la respuesta es correcta, cerramos la ActividadAlumno y calculamos nota/puntuación.
        if (Boolean.TRUE.equals(correcta)) {
            actividadAlumno.setAcabada(LocalDateTime.now());
            actividadAlumno.setPuntuacion(ordenacion.getPuntuacion());

            int notaCalculada = 10 - (numFallosAntes == null ? 0 : numFallosAntes.intValue());
            if (notaCalculada < 1) {
                notaCalculada = 1;
            }
            actividadAlumno.setNota(notaCalculada);

            actividadAlumnoRepository.save(actividadAlumno);
        }

        RespAlumnoOrdenacionDTO dto = new RespAlumnoOrdenacionDTO(respAlumnoOrdenacion.getId(), respAlumnoOrdenacion.getCorrecta());
        return new RespAlumnoOrdenacionCreateResponse(dto, comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public RespAlumnoOrdenacion readRespAlumnoOrdenacion(Long id) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno a la actividad de ordenación no existe"));
        return respAlumnoOrdenacion;
        
    }

    @Override
    public Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(respuestaAlumnoId)
                .orElseThrow(() -> new RuntimeException("La respuesta del alumno para la actividad de ordenación no existe"));
        Boolean esCorrecta = false;

        if (respAlumnoOrdenacion.getValoresAlum().equals(respAlumnoOrdenacion.getOrdenacion().getValores())) {
            esCorrecta = true;
        }

        respAlumnoOrdenacion.setCorrecta(esCorrecta);
        respAlumnoOrdenacionRepository.save(respAlumnoOrdenacion);
        return esCorrecta;
    }

    @Override
    public Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(respuestaAlumnoId)
                .orElseThrow(() -> new RuntimeException("La respuesta del alumno para la actividad de ordenación no existe"));
        List<String> valoresAlumno = respAlumnoOrdenacion.getValoresAlum();
        List<String> valoresCorrectos = respAlumnoOrdenacion.getOrdenacion().getValores();

        Integer numPosicionesCorrectas = 0;
        for (int i = 0; i < Math.min(valoresAlumno.size(), valoresCorrectos.size()); i++) {
            if (valoresAlumno.get(i).equals(valoresCorrectos.get(i))) {
                numPosicionesCorrectas++;
            }
        }

        return numPosicionesCorrectas;
    }
}
