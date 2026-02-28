package com.cerebrus.actividad;

import java.util.List;

public interface OrdenacionService {
    Ordenacion crearActOrdenacion(String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores);
    Ordenacion readOrdenacion(Long id);
    Ordenacion updateActOrdenacion(Long id, String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores);
    void deleteActOrdenacion(Long id);
}
