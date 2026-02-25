package com.cerebrus.curso;

import java.util.List;

public interface CursoService {
    List<String> obtenerDetallesCurso(Long id);
    List<Curso> obtenerCursosUsuarioLogueado();
    Curso crearCurso(String titulo, String descripcion, String imagen);
}
