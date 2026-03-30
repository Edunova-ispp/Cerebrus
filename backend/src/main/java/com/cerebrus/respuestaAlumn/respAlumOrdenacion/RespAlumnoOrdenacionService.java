package com.cerebrus.respuestaAlumn.respAlumOrdenacion;

import java.util.List;

import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionCreateResponse;

public interface RespAlumnoOrdenacionService {

    RespAlumnoOrdenacionCreateResponse crearRespuestaAlumnoOrdenacion(Long actAlumnoId, List<String> valoresAlum, Long actOrdId);
    RespAlumnoOrdenacion encontrarRespuestaAlumnoOrdenacionPorId(Long id);
    Boolean corregirRespuestaAlumnoOrdenacion(Long respuestaAlumnoId);
    Integer obtenerNumPosicionesCorrectas(Long respuestaAlumnoId);

}
