package com.cerebrus.respuestaMaestro;

import java.util.List;

public interface RespuestaMaestroService {
    RespuestaMaestro crearRespuesta(String respuesta, String imagen, Boolean correcta, Long preguntaId);
    RespuestaMaestro readRespuesta(Long id);
    RespuestaMaestro updateRespuesta(Long id, String respuesta, String imagen, Boolean correcta);
    void deleteRespuesta(Long id);
    List<RespuestaMaestro> encontrarRespuestasPorPreguntaId(Long preguntaId);
}
