package com.cerebrus.respuestaAlumno.respAlumPuntoImagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.marcarImagen.MarcarImagenService;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoimagen.PuntoImagen;
import com.cerebrus.puntoimagen.PuntoImagenService;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class RespAlumnoPuntoImagenServiceImpl implements RespAlumnoPuntoImagenService {

    private final RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository;
    private final PuntoImagenService puntoImagenService;
    private final MarcarImagenService marcarImagenService;
    private final UsuarioService usuarioService;

    @Autowired
    public RespAlumnoPuntoImagenServiceImpl(RespAlumnoPuntoImagenRepository respAlumnoPuntoImagenRepository, PuntoImagenService puntoImagenService, MarcarImagenService marcarImagenService, UsuarioService usuarioService) {
        this.respAlumnoPuntoImagenRepository = respAlumnoPuntoImagenRepository;
        this.puntoImagenService = puntoImagenService;
        this.marcarImagenService = marcarImagenService;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public RespAlumnoPuntoImagen crearRespuestaAlumnoPuntoImagen(String respuesta, Integer pixelX, Integer pixelY, Long marcarImagenId, Long actividadAlumnoId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un alumno puede crear respuestas para puntos de imagen");
        }

        RespAlumnoPuntoImagen respAlumnoPuntoImagen = new RespAlumnoPuntoImagen();
        MarcarImagen marcarImagen = marcarImagenService.obtenerMarcarImagenPorId(marcarImagenId);
        ActividadAlumno actividadAlumno = respAlumnoPuntoImagenRepository.encontrarActividadAlumnoPorId(actividadAlumnoId); 
        respAlumnoPuntoImagen.setRespuesta(respuesta);
        respAlumnoPuntoImagen.setPixelX(pixelX);
        respAlumnoPuntoImagen.setPixelY(pixelY);
        respAlumnoPuntoImagen.setMarcarImagen(marcarImagen);
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
        PuntoImagen puntoImagenRespuesta = puntoImagenService.encontrarPuntoImagenPorCoordenada(respuestaAlumno.getMarcarImagen().getId(), respuestaAlumno.getPixelX(), respuestaAlumno.getPixelY());
        Boolean correcta = puntoImagenRespuesta.getRespuesta().trim().toLowerCase().equals(respuestaAlumno.getRespuesta().trim().toLowerCase());
        respuestaAlumno.setCorrecta(correcta);
        respAlumnoPuntoImagenRepository.save(respuestaAlumno);
        return correcta;
    }
}
