package com.cerebrus.respuestaAlumno.respAlumPuntoImagen;

public interface RespAlumnoPuntoImagenService {

    RespAlumnoPuntoImagen encontrarRespuestaAlumnoPuntoImagenPorId(Long id);
    Boolean corregirRespuestaAlumnoPuntoImagen(Long id);
    RespAlumnoPuntoImagen crearRespuestaAlumnoPuntoImagen(String respuesta, Long puntoImagenId, Long actividadAlumnoId);

}
