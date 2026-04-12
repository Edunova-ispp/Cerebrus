package com.cerebrus.tema;

import java.util.List;

public interface TemaService {

    Tema crearTema(String titulo, Long cursoId, Long maestroId);
    Tema encontrarTemaPorId(Long temaId);
    List<Tema> encontrarTemasPorCursoAlumnoId(Long cursoId);
    List<Tema> encontrarTemasPorCursoMaestroId(Long cursoId);
    void eliminarTemaPorId(Long temaId);
    Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId);

}