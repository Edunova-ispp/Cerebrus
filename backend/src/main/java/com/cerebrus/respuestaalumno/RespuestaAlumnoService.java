package com.cerebrus.respuestaalumno;

public interface RespuestaAlumnoService {

    RespuestaAlumno encontrarRespuestaAlumnoPorId(Long id);
    RespuestaAlumno marcarODesmarcarRespuestaCorrecta(Long id);

}
