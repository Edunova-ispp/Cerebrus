package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import java.util.List;

import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionCreateResponse;

public interface RespAlumnoOrdenacionService {
    RespAlumnoOrdenacionCreateResponse crearRespAlumnoOrdenacion(Long actAlumnoId, List<String> valoresAlum, Long actOrdId);
    RespAlumnoOrdenacion readRespAlumnoOrdenacion(Long id);
    Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId);
    Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId);

}
