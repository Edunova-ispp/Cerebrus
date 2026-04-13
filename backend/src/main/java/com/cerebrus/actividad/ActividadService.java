package com.cerebrus.actividad;

import java.util.List;

public interface ActividadService {

    Actividad crearActTeoria(String titulo, String descripcion, String imagen, Long temaId, Boolean permitirReintento);
    Actividad encontrarActTeoriaPorId(Long id);
    Actividad encontrarActTeoriaMaestroPorId(Long id);
    List<Actividad> encontrarActividadesPorTema(Long temaId);
    Actividad actualizarActTeoria(Long id, String titulo, String descripcion, String imagen, Boolean permitirReintento);
    void eliminarActTeoriaPorId(Long id);

}
