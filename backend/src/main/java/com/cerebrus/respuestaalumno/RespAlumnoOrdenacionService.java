package com.cerebrus.respuestaalumno;

public interface RespAlumnoOrdenacionService {

    Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId);
    Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId);

}
