package com.cerebrus.respuestaMaestro;

import java.util.List;

public interface RespuestaMaestroService {
    RespuestaMaestro crearRespuestaMaestro(String respuesta, String imagen, Boolean correcta, Long preguntaId);
    RespuestaMaestro encontrarRespuestaMaestroPorId(Long id);
    List<RespuestaMaestro> encontrarRespuestasMaestroPorPreguntaId(Long preguntaId);
    RespuestaMaestro actualizarRespuestaMaestro(Long id, String respuesta, String imagen, Boolean correcta);
    void eliminarRespuestaMaestroPorId(Long id);
}
