package com.cerebrus.respuestaalumno;

import java.util.List;

public interface RespAlumnoOrdenacionService {
    RespAlumnoOrdenacionCreateResponse crearRespAlumnoOrdenacion(Long actAlumnoId, List<String> valoresAlum, Long actOrdId);
    RespAlumnoOrdenacion readRespAlumnoOrdenacion(Long id);
    Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId);
    Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId);

}
