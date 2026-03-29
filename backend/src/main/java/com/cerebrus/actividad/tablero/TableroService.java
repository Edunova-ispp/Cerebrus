package com.cerebrus.actividad.tablero;

import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.actividad.tablero.dto.TableroRequest;

public interface TableroService {

    TableroDTO crearActTablero(TableroRequest actividad);
    TableroDTO encontrarActTableroPorId(Long tableroId);
    TableroDTO actualizarActTablero(Long id, TableroRequest tablero);
    void eliminarActTableroPorId(Long tableroId);
    String crearRespuestaAPreguntaEnActTablero(String respuesta, Long tableroId, Long preguntaId);

}
