package com.cerebrus.puntoImagen;

import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;

public interface PuntoImagenService {

    PuntoImagen crearPuntoImagen(PuntoImagenDTO puntoImagenDTO, MarcarImagen marcarImagen);
    PuntoImagen encontrarPuntoImagenPorId(Long id);
    PuntoImagen encontrarPuntoImagenPorCoordenada(Long marcarImagenId, Integer pixelX, Integer pixelY);
    PuntoImagen actualizarPuntoImagen(PuntoImagenDTO puntoImagenDTO);
    void eliminarPuntoImagenPorId(Long id);

}
