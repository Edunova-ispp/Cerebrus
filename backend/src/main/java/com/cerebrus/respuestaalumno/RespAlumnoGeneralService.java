package com.cerebrus.respuestaalumno;

public interface RespAlumnoGeneralService {
    RespAlumnoGeneralCreateResponse crearRespAlumnoGeneral(Long actAlumnoId, String respuesta, Long preguntaId);
    RespAlumnoGeneral readRespAlumnoGeneral(Long id);
    RespAlumnoGeneral updateRespAlumnoGeneral(Long id, Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId);
    void deleteRespAlumnoGeneral(Long id);
}
