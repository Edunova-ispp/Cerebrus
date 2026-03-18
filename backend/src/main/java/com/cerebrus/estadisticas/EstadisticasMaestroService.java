package com.cerebrus.estadisticas;

import java.util.Map;

import com.cerebrus.curso.Curso;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;

public interface EstadisticasMaestroService {
    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
    Map<String, Integer> calcularTotalPuntosCursoPorAlumno(Long cursoId);
    Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(Long cursoId, Long temaId);
    Map<Long, EstadisticasTemaDTO> obtenerEstadisticasCursoTema(Long cursoId);
    Integer obtenerNotaMaximaCurso(Long cursoId);
    Integer obtenerNotaMinimaCurso(Long cursoId);
}
