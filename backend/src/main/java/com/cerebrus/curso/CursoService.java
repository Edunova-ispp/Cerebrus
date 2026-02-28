package com.cerebrus.curso;

import java.util.List;

public interface CursoService {
    List<String> obtenerDetallesCurso(Long id);
    List<Curso> ObtenerCursosUsuarioLogueado();
    Curso crearCurso(String titulo, String descripcion, String imagen);
    Curso cambiarVisibilidad(Long id);
    ProgresoDTO getProgreso(Long cursoId);
}
