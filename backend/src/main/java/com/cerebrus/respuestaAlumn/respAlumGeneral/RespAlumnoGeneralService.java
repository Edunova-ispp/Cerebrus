package com.cerebrus.respuestaAlumn.respAlumGeneral;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface RespAlumnoGeneralService {
    RespAlumnoGeneralCreateResponse crearRespAlumnoGeneral(Long actAlumnoId, Long respuestaId, Long preguntaId);
    RespAlumnoGeneral readRespAlumnoGeneral(Long id);
    RespAlumnoGeneral updateRespAlumnoGeneral(Long id, Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId);
    void deleteRespAlumnoGeneral(Long id);
    Boolean corregirRespuestaAlumnoGeneral(Long id);
    boolean corregirRespuestaAlumnoGeneralClasificacion(Long id);
    HashMap<Long, String> corregirCrucigrama(LinkedHashMap<Long,String> respuestas, Long crucigramaId);
    EvaluacionActividadAbiertaResponse corregirActividadAbierta(EvaluacionActividadAbiertaRequest request);}
