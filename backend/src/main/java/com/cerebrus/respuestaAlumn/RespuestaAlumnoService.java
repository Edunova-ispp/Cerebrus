package com.cerebrus.respuestaAlumn;

public interface RespuestaAlumnoService {

    RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id);
    RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id);

}
