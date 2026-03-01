package com.cerebrus.tema;

import java.util.List;

public interface TemaService {

    List<Tema> ObtenerTemasPorCursoAlumno(Long cursoId);

    List<Tema> ObtenerTemasPorCursoMaestro(Long cursoId);

    Tema crearTema(String titulo, Long cursoId, Long maestroId);

    Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId);

    void eliminarTema(Long temaId);
}