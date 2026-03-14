package com.cerebrus.actividad.marcarImagen;

import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;

public interface MarcarImagenService {

    MarcarImagen crearMarcarImagen(MarcarImagenDTO marcarImagen);
    MarcarImagen obtenerMarcarImagenPorId(Long id);
    MarcarImagen actualizarMarcarImagen(Long id, MarcarImagenDTO marcarImagenDTO);
    void eliminarMarcarImagen(Long id);

}
