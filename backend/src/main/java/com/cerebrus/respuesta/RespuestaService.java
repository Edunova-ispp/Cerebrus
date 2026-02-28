package com.cerebrus.respuesta;

public interface RespuestaService {
    Respuesta crearRespuesta(String respuesta, String imagen, Boolean correcta, Long preguntaId);
    Respuesta readRespuesta(Long id);
    Respuesta updateRespuesta(Long id, String respuesta, String imagen, Boolean correcta);
    void deleteRespuesta(Long id);
}
