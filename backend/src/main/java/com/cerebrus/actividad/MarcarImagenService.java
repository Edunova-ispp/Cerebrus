package com.cerebrus.actividad;

public interface MarcarImagenService {

    MarcarImagen crearMarcarImagen(MarcarImagenDTO marcarImagen);
    MarcarImagen obtenerMarcarImagenPorId(Long id);
    MarcarImagen actualizarMarcarImagen(Long id, MarcarImagenDTO marcarImagenDTO);
    void eliminarMarcarImagen(Long id);

}
