package com.cerebrus.estadisticas;

import java.util.HashMap;
import java.util.Map;

import com.cerebrus.curso.Curso;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;

public interface EstadisticasMaestroService {
    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
    HashMap<String, Integer> calcularTotalPuntosCursoPorAlumno(Long cursoId);
    Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(Long cursoId, Long temaId);
    Map<Long, EstadisticasTemaDTO> obtenerEstadisticasCursoTema(Long cursoId);
    Integer obtenerNotaMaximaCurso(Long cursoId);
    Integer obtenerNotaMinimaCurso(Long cursoId);
    Integer obtenerTiempoAlumnoEnActividad(Long alumnoId, Long actividadId);
    Integer obtenerTiempoAlumnoEnTema(Long alumnoId, Long temaId);
    Integer obtenerTiempoAlumnoEnCurso(Long alumnoId, Long cursoId);
    Double obtenerTiempoMedioActividad(Long actividadId);
    Double obtenerTiempoMedioTema(Long temaId);
    Double obtenerTiempoMedioCurso(Long cursoId);
    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosActividad(Long actividadId, Integer limite);
    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosTema(Long temaId, Integer limite);
    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosCurso(Long cursoId, Integer limite);
}
