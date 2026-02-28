package com.cerebrus.inscripcion;

import java.util.List;

public interface InscripcionService {
    Inscripcion CrearInscripcion(String codigoCurso);
    List<InscripcionResumenDTO> obtenerMisInscripciones();
}
