package com.cerebrus.respuestaalumno;

import java.util.List;

public interface RespAlumnoOrdenacionService {
    RespAlumnoOrdenacionCreateResponse crearRespAlumnoOrdenacion(Long actAlumnoId, List<String> valoresAlum, Long actOrdId);
    RespAlumnoOrdenacion readRespAlumnoOrdenacion(Long id);
}
