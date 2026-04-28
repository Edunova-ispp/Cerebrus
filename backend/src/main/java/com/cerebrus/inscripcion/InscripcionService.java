package com.cerebrus.inscripcion;

import java.util.List;

import com.cerebrus.inscripcion.dto.AlumnoCursoDTO;
import com.cerebrus.inscripcion.dto.InscripcionRequestDTO;
import com.cerebrus.inscripcion.dto.InscripcionResponseDTO;

public interface InscripcionService {
    Inscripcion crearInscripcion(String codigoCurso);
    List<AlumnoCursoDTO> listarInscripcionesPorCurso(Long cursoId);
    void expulsarAlumno(Long cursoId, Long alumnoId);
    InscripcionResponseDTO inscribirMultiplesAlumnos(Long cursoId, InscripcionRequestDTO request);
}

