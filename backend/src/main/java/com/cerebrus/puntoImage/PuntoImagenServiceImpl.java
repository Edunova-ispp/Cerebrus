package com.cerebrus.puntoImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.puntoImage.dto.PuntoImagenDTO;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

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
        if (puntoImagenDTO == null) {
            throw new IllegalArgumentException("El punto de imagen es obligatorio");
        }
        if (marcarImagen == null) {
            throw new IllegalArgumentException("La actividad marcar imagen es obligatoria");
        }
        String respuestaNormalizada = normalizeRespuesta(puntoImagenDTO.getRespuesta());
        validateCoordenada(puntoImagenDTO.getPixelX(), "pixelX");
        validateCoordenada(puntoImagenDTO.getPixelY(), "pixelY");

        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setPixelX(puntoImagenDTO.getPixelX());
        puntoImagen.setPixelY(puntoImagenDTO.getPixelY());
        puntoImagen.setRespuesta(respuestaNormalizada);
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
        if (puntoImagenDTO == null) {
            throw new IllegalArgumentException("El punto de imagen es obligatorio");
        }
        String respuestaNormalizada = normalizeRespuesta(puntoImagenDTO.getRespuesta());

        PuntoImagen puntoImagen = obtenerPuntoImagenPorId(puntoImagenDTO.getId());
        puntoImagen.setRespuesta(respuestaNormalizada);
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
        validateCoordenada(pixelX, "pixelX");
        validateCoordenada(pixelY, "pixelY");
        return puntoImagenRepository.findByMarcarImagenIdAndPixelXAndPixelY(marcarImagenId, pixelX, pixelY)
                .orElseThrow(() -> new ResourceNotFoundException("PuntoImagen", "marcarImagenId, pixelX, pixelY", marcarImagenId + ", " + pixelX + ", " + pixelY));
    }

    private void validateCoordenada(Integer value, String field) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("El " + field + " no puede ser negativo");
        }
    }

    private String normalizeRespuesta(String respuesta) {
        if (respuesta == null) {
            throw new IllegalArgumentException("La respuesta es obligatoria");
        }
        String normalizada = respuesta.trim();
        if (normalizada.isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacia");
        }
        return normalizada;
    }
}
