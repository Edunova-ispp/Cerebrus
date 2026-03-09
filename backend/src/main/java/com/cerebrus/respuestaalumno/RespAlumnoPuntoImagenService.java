package com.cerebrus.respuestaalumno;

public interface RespAlumnoPuntoImagenService {

    RespAlumnoPuntoImagen encontrarRespuestaAlumnoPuntoImagenPorId(Long id);
    Boolean corregirRespuestaAlumnoPuntoImagen(Long id);
    RespAlumnoPuntoImagen crearRespuestaAlumnoPuntoImagen(String respuesta, Integer pixelX, Integer pixelY, Long marcarImagenId, Long actividadAlumnoId);

}
