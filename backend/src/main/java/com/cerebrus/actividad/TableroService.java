package com.cerebrus.actividad;

import jakarta.validation.Valid;

public interface TableroService {

    TableroDTO crearActividadTablero(TableroRequest actividad);

    TableroDTO getTablero(Long tableroId);

    void eliminarTablero(Long tableroId);

    TableroDTO actualizarTablero(Long id, TableroRequest tablero);

    String crearRespuestaAPreguntaTablero(String respuesta, Long tableroId, Long preguntaId);

}
