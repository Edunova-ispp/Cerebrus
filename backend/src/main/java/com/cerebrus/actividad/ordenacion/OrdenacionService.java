package com.cerebrus.actividad.ordenacion;

import java.util.List;

public interface OrdenacionService {
    Ordenacion crearActOrdenacion(String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores);
    Ordenacion encontrarActOrdenacionPorId(Long id);
    Ordenacion encontrarActOrdenacionMaestroPorId(Long id);
    Ordenacion actualizarActOrdenacion(Long id, String titulo, String descripcion, 
        Integer puntuacion, String imagen, Long temaId, Boolean respVisible, 
        String comentariosRespVisible, Integer posicion, List<String> valores);
    void eliminarActOrdenacionPorId(Long id);
}
