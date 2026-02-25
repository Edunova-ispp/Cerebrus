package com.cerebrus.curso;

import java.util.List;

public interface CursoService {
    List<String> obtenerDetallesCurso(Long id);
    List<Curso> ObtenerCursosUsuarioLogueado();
}
