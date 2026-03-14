package com.cerebrus.respuestaAlumno;

public interface RespuestaAlumnoService {

    RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id);
    RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id);

}
