package com.cerebrus.puntoimagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.MarcarImagen;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@Service
@Transactional
public class PuntoImagenServiceImpl implements PuntoImagenService {

    private final PuntoImagenRepository puntoImagenRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public PuntoImagenServiceImpl(PuntoImagenRepository puntoImagenRepository, UsuarioService usuarioService) {
        this.puntoImagenRepository = puntoImagenRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public PuntoImagen crearPuntoImagen(PuntoImagenDTO puntoImagenDTO, MarcarImagen marcarImagen) {
        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setPixelX(puntoImagenDTO.getPixelX());
        puntoImagen.setPixelY(puntoImagenDTO.getPixelY());
        puntoImagen.setRespuesta(puntoImagenDTO.getRespuesta());
        puntoImagen.setMarcarImagen(marcarImagen);
        return puntoImagenRepository.save(puntoImagen);
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoImagen obtenerPuntoImagenPorId(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro) && !(u instanceof Alumno)) {
            throw new AccessDeniedException("Solo un usuario logueado como alumno o maestro puede obtener los puntos de la imagen");
        }

        return puntoImagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PuntoImagen", "id", id));
    }

    @Override
    public PuntoImagen actualizarPuntoImagen(PuntoImagenDTO puntoImagenDTO) {
        PuntoImagen puntoImagen = obtenerPuntoImagenPorId(puntoImagenDTO.getId());
        puntoImagen.setRespuesta(puntoImagenDTO.getRespuesta());
        return puntoImagenRepository.save(puntoImagen);
    }

    @Override
    @Transactional
    public void eliminarPuntoImagen(Long id) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Maestro)) {
            throw new AccessDeniedException("Solo un maestro puede eliminar puntos de la imagen");
        }

        PuntoImagen puntoImagen = obtenerPuntoImagenPorId(id);
        puntoImagenRepository.delete(puntoImagen);
    }

    @Override
    public PuntoImagen encontrarPuntoImagenPorCoordenada(Long marcarImagenId, Integer pixelX, Integer pixelY) {
        return puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(marcarImagenId, pixelX, pixelY)
                .orElseThrow(() -> new ResourceNotFoundException("PuntoImagen", "marcarImagenId, pixelX, pixelY", marcarImagenId + ", " + pixelX + ", " + pixelY));
    }
}
