package com.cerebrus.tema;

import java.util.List;

public interface TemaService {

    List<Tema> ObtenerTemasPorCursoAlumno(Integer cursoId);

    List<Tema> ObtenerTemasPorCursoMaestro(Integer cursoId);

    Tema crearTema(String titulo, Long cursoId, Long maestroId);

    Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId);
}