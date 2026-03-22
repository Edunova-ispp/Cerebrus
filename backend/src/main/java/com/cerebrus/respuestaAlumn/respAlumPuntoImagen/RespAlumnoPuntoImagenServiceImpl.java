package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImage.PuntoImagen;
import com.cerebrus.puntoImage.PuntoImagenService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;


@Service
@Transactional
public class RespAlumnoPuntoImagenServiceImpl implements RespAlumnoPuntoImagenService {

    private final RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository;
    private final PuntoImagenService puntoImagenService;
    private final UsuarioService usuarioService;

    @Autowired
    public RespAlumnoPuntoImagenServiceImpl(RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository, PuntoImagenService puntoImagenService, UsuarioService usuarioService) {
        this.respAlumnoPuntoImagenRepository = respAlumnoPuntoImagenRepository;
        this.puntoImagenService = puntoImagenService;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public RespAlumnoPuntoImagen crearRespuestaAlumnoPuntoImagen(String respuesta, Long puntoImagenId, Long actividadAlumnoId) {
        String respuestaNormalizada = respuesta == null ? "" : respuesta.trim();
        if (respuestaNormalizada.isEmpty()) {
            throw new IllegalArgumentException("La respuesta es obligatoria");
        }

        if (puntoImagenId == null) {
            throw new IllegalArgumentException("El punto de imagen es obligatorio");
        }

        if (actividadAlumnoId == null) {
            throw new IllegalArgumentException("La actividad del alumno es obligatoria");
        }

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas para puntos de imagen");
        }

        RespAlumnoPuntoImagen respAlumnoPuntoImagen = new RespAlumnoPuntoImagen();
        PuntoImagen puntoImagen = puntoImagenService.obtenerPuntoImagenPorId(puntoImagenId);
        ActividadAlumno actividadAlumno = respAlumnoPuntoImagenRepository.encontrarActividadAlumnoPorId(actividadAlumnoId);
        if (actividadAlumno == null) {
            throw new ResourceNotFoundException("ActividadAlumno", "id", actividadAlumnoId);
        }
        respAlumnoPuntoImagen.setRespuesta(respuestaNormalizada);
        respAlumnoPuntoImagen.setPuntoImagen(puntoImagen);
        respAlumnoPuntoImagen.setActividadAlumno(actividadAlumno);
        respAlumnoPuntoImagen.setCorrecta(false);
        return respAlumnoPuntoImagenRepository.save(respAlumnoPuntoImagen);
    }

    @Override
    public RespAlumnoPuntoImagen encontrarRespuestaAlumnoPuntoImagenPorId(Long id) {
        return respAlumnoPuntoImagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RespAlumnoPuntoImagen", "id", id));
    }

    @Override
    public Boolean corregirRespuestaAlumnoPuntoImagen(Long id) {
        RespAlumnoPuntoImagen respuestaAlumno = encontrarRespuestaAlumnoPuntoImagenPorId(id);
        PuntoImagen puntoImagenRespuesta = respuestaAlumno.getPuntoImagen();
        Boolean correcta = puntoImagenRespuesta.getRespuesta().trim().toLowerCase().equals(respuestaAlumno.getRespuesta().trim().toLowerCase());
        respuestaAlumno.setCorrecta(correcta);
        respAlumnoPuntoImagenRepository.save(respuestaAlumno);
        return correcta;
    }
}
