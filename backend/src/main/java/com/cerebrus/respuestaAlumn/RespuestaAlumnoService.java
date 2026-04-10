package com.cerebrus.respuestaAlumn;

import com.cerebrus.respuestaAlumn.dto.RespuestasActividadDTO;

public interface RespuestaAlumnoService {

    RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id);
    RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id);
    RespuestasActividadDTO obtenerRespuestasActividadAlumno(Long actividadId);

}
