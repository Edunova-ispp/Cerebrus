package com.cerebrus.curso;

import java.util.List;
import java.util.Map;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.usuario.Alumno;

public interface CursoService {
    List<String> obtenerDetallesCurso(Long id);
    List<Curso> ObtenerCursosUsuarioLogueado();
    Curso crearCurso(String titulo, String descripcion, String imagen);
    Map<Alumno, Integer> calcularTotalPuntosCursoPorAlumno(Curso curso);
    Curso getCursoById(Long id);
    Curso cambiarVisibilidad(Long id);
    ProgresoDTO getProgreso(Long cursoId);
    Curso actualizarCurso(Long id, String titulo, String descripcion, String imagen);
    Map<Integer,Integer> getNotaMediaPorActividad(Long cursoId);
    Map<Actividad,Double> CalcularNotaMediaActividadMasAlta(Long cursoId);
    Map<Actividad,Double> CalcularNotaMediaActividadMasBaja(Long cursoId);
}
