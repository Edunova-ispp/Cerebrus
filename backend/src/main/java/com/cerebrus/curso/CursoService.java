package com.cerebrus.curso;

import java.util.List;
import java.util.Map;

import com.cerebrus.usuario.Alumno;

public interface CursoService {
    List<String> obtenerDetallesCurso(Long id);
    List<Curso> ObtenerCursosUsuarioLogueado();
    Curso crearCurso(String titulo, String descripcion, String imagen);
    Curso getCursoById(Long id);
    Curso cambiarVisibilidad(Long id);
    ProgresoDTO getProgreso(Long cursoId);
    Curso actualizarCurso(Long id, String titulo, String descripcion, String imagen);
}
