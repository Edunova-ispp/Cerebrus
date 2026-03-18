package com.cerebrus.estadisticas;

import java.util.Map;

import com.cerebrus.curso.Curso;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;

public interface EstadisticasMaestroService {
    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
    Map<String, Integer> calcularTotalPuntosCursoPorAlumno(Long cursoId);
    Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(Long cursoId, Long temaId);
}
