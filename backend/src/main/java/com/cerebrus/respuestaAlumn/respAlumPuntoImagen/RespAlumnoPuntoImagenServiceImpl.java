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
        Usuario current = usuarioService.findCurrentUser();
        if (!(current instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas para puntos de imagen");
        }

        RespAlumnoPuntoImagen respAlumnoPuntoImagen = new RespAlumnoPuntoImagen();
        PuntoImagen puntoImagen = puntoImagenService.obtenerPuntoImagenPorId(puntoImagenId);
        ActividadAlumno actividadAlumno = respAlumnoPuntoImagenRepository.encontrarActividadAlumnoPorId(actividadAlumnoId); 
        if (actividadAlumno.getAlumno() == null || actividadAlumno.getAlumno().getId() == null
            || !actividadAlumno.getAlumno().getId().equals(current.getId())) {
            throw new AccessDeniedException("No puedes crear una respuesta para una ActividadAlumno que no es tuya");
        }
        
        respAlumnoPuntoImagen.setRespuesta(respuesta);
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
