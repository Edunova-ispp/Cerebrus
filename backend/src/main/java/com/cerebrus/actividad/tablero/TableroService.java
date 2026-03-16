package com.cerebrus.actividad.tablero;

import com.cerebrus.actividad.tablero.dto.TableroDTO;

public interface TableroService {

    TableroDTO crearActividadTablero(TableroRequest actividad);

    TableroDTO getTablero(Long tableroId);

    void eliminarTablero(Long tableroId);

    TableroDTO actualizarTablero(Long id, TableroRequest tablero);

    String crearRespuestaAPreguntaTablero(String respuesta, Long tableroId, Long preguntaId);

}
