package com.cerebrus.curso;

import java.util.List;

import com.cerebrus.curso.dto.ProgresoDTO;

public interface CursoService {
    Curso crearCurso(String titulo, String descripcion, String imagen);
    Curso encontrarCursoPorId(Long id);
    List<String> encontrarDetallesCursoPorId(Long id);
    List<Curso> encontrarCursosPorUsuarioLogueado();
    Curso actualizarCurso(Long id, String titulo, String descripcion, String imagen);
    void eliminarCursoPorId(Long id);
    ProgresoDTO encontrarProgresoPorCursoId(Long cursoId);
    Curso cambiarVisibilidad(Long id);
    List<Integer> obtenerNotaMediaPorActividadPorCursoId(Long cursoId);
}
