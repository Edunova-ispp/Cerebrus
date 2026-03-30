package com.cerebrus.actividad.marcarImagen;

import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;

public interface MarcarImagenService {

    MarcarImagen crearActMarcarImagen(MarcarImagenDTO marcarImagen);
    MarcarImagen encontrarActMarcarImagenPorId(Long id);
    MarcarImagen actualizarActMarcarImagen(Long id, MarcarImagenDTO marcarImagenDTO);
    void eliminarActMarcarImagenPorId(Long id);

}
