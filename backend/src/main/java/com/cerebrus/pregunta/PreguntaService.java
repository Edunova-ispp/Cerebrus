package com.cerebrus.pregunta;

public interface PreguntaService {
    Pregunta crearPregunta(String pregunta, String imagen, Long actId);
    Pregunta encontrarPreguntaPorId(Long id);
    Pregunta actualizarPregunta(Long id, String pregunta, String imagen);
    void eliminarPreguntaPorId(Long id);
}
