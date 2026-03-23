package com.cerebrus.puntoImage;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.puntoImage.dto.PuntoImagenDTO;

public interface PuntoImagenService {


    PuntoImagen crearPuntoImagen(PuntoImagenDTO puntoImagenDTO, MarcarImagen marcarImagen);
    PuntoImagen obtenerPuntoImagenPorId(Long id);
    PuntoImagen actualizarPuntoImagen(PuntoImagenDTO puntoImagenDTO);
    void eliminarPuntoImagen(Long id);
    PuntoImagen encontrarPuntoImagenPorCoordenada(Long marcarImagenId, Integer pixelX, Integer pixelY);

}
