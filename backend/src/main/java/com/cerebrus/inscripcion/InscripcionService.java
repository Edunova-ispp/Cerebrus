package com.cerebrus.inscripcion;

import java.util.List;

public interface InscripcionService {
    Inscripcion crearInscripcion(String codigoCurso);
    List<Inscripcion> listarInscripcionesPorCurso(Long cursoId);
    void expulsarAlumno(Long cursoId, Long alumnoId);
}
