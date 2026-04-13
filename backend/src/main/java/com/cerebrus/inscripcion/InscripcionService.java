package com.cerebrus.inscripcion;

import java.util.List;

import com.cerebrus.inscripcion.dto.AlumnoCursoDTO;

public interface InscripcionService {
    Inscripcion crearInscripcion(String codigoCurso);
    List<AlumnoCursoDTO> listarInscripcionesPorCurso(Long cursoId);
    void expulsarAlumno(Long cursoId, Long alumnoId);
}
