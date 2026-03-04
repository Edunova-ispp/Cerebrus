package com.cerebrus.pregunta;

public interface PreguntaService {
    Pregunta crearPregunta(String pregunta, String imagen, Long actId);
    Pregunta readPregunta(Long id);
    Pregunta updatePregunta(Long id, String pregunta, String imagen);
    void deletePregunta(Long id);
}
