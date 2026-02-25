package com.cerebrus.tema;

import java.util.List;

public interface TemaService {

    public List<Tema> ObtenerTemasPorCursoAlumno(Integer cursoId);

    Tema crearTema(String titulo, Long cursoId, Long maestroId);

    Tema renombrarTema(Long temaId, String nuevoTitulo, Long maestroId);
}
