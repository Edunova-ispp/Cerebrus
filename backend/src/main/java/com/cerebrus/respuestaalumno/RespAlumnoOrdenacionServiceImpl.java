package com.cerebrus.respuestaalumno;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.Ordenacion;
import com.cerebrus.actividad.OrdenacionRepository;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;


@Service
@Transactional
public class RespAlumnoOrdenacionServiceImpl implements RespAlumnoOrdenacionService {

    private final RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository;
    private final UsuarioService usuarioService;
    private final ActividadAlumnoRepository actividadAlumnoRepository;
    private final OrdenacionRepository ordenacionRepository;

    @Autowired
    public RespAlumnoOrdenacionServiceImpl(RespAlumnoOrdenacionRepository respAlumnoOrdenacionRepository, UsuarioService usuarioService,
         ActividadAlumnoRepository actividadAlumnoRepository, OrdenacionRepository ordenacionRepository) {
        this.respAlumnoOrdenacionRepository = respAlumnoOrdenacionRepository;
        this.usuarioService = usuarioService;
        this.actividadAlumnoRepository = actividadAlumnoRepository;
        this.ordenacionRepository = ordenacionRepository;
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

        return new RespAlumnoOrdenacionCreateResponse(respAlumnoOrdenacion, comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public RespAlumnoOrdenacion readRespAlumnoOrdenacion(Long id) {
        RespAlumnoOrdenacion respAlumnoOrdenacion = respAlumnoOrdenacionRepository.findById(id).orElseThrow(() -> new RuntimeException("La respuesta del alumno a la actividad de ordenación no existe"));
        return respAlumnoOrdenacion;
    }
}
