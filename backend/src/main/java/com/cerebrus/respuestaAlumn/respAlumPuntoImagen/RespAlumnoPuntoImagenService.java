package com.cerebrus.respuestaAlumn.respAlumPuntoImagen;

public interface RespAlumnoPuntoImagenService {

    RespAlumnoPuntoImagen crearRespuestaAlumnoPuntoImagen(String respuesta, Long puntoImagenId, Long actividadAlumnoId);
    RespAlumnoPuntoImagen encontrarRespuestaAlumnoPuntoImagenPorId(Long id);
    Boolean corregirRespuestaAlumnoPuntoImagen(Long id);

}
