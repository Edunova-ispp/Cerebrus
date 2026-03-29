package com.cerebrus.estadisticas;

import java.util.HashMap;
import java.util.Map;

import com.cerebrus.curso.Curso;
import java.util.List;

import com.cerebrus.estadisticas.dto.AlumnoBasicoDTO;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoResumenDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;

public interface EstadisticasMaestroService {

    EstadisticasCursoDTO obtenerEstadisticasCurso(Long cursoId);
    Map<Long, EstadisticasTemaDTO> obtenerEstadisticasCursoTema(Long cursoId);
    Map<Long, EstadisticasActividadDTO> obtenerEstadisticasCursoActividad(Long cursoId, Long temaId);

    Map<String, Long> numActividadesRealizadasPorAlumno(Curso curso);
    HashMap<String, Integer> calcularTotalPuntosCursoPorAlumno(Long cursoId);
    Map<Long, RepeticionesActividadDTO> obtenerRepeticionesCursoActividad(Long cursoId, Long temaId);

    Integer obtenerTiempoAlumnoEnCurso(Long alumnoId, Long cursoId);
    Integer obtenerTiempoAlumnoEnTema(Long alumnoId, Long temaId);
    Integer obtenerTiempoAlumnoEnActividad(Long alumnoId, Long actividadId);

    Double obtenerTiempoMedioCurso(Long cursoId);
    Double obtenerTiempoMedioTema(Long temaId);
    Double obtenerTiempoMedioActividad(Long actividadId);

    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosCurso(Long cursoId, Integer limite);
    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosTema(Long temaId, Integer limite);
    AlumnosMasRapidosLentosDTO obtenerAlumnosMasRapidosLentosActividad(Long actividadId, Integer limite);

    Boolean temaCompletado(Long alumnoId, Long cursoId, Long temaId);
    Integer notaMediaAlumno(Long alumnoId, Long cursoId, Long temaId);

    Map<Long, EstadisticasAlumnoDTO> obtenerEstadisticasAlumno(Long alumnoId, Long cursoId, Long temaId);
    RepeticionesActividadDTO obtenerRepeticionesActividad(Long actividadId);
    EstadisticasAlumnoResumenDTO obtenerResumenEstadisticasAlumno(Long cursoId, Long alumnoId);

}
