package com.cerebrus.respuestaAlumn.respAlumGeneral;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaRequest;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;

public interface RespAlumnoGeneralService {
    RespAlumnoGeneralCreateResponse crearRespuestaAlumnoGeneral(Long actAlumnoId, Long respuestaId, Long preguntaId);
    RespAlumnoGeneral encontrarRespuestaAlumnoGeneralPorId(Long id);
    RespAlumnoGeneral actualizarRespuestaAlumnoGeneral(Long id, Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId);
    void eliminarRespuestaAlumnoGeneralPorId(Long id);
    Boolean corregirRespuestaAlumnoGeneral(Long id);
    boolean corregirRespuestaAlumnoGeneralClasificacion(Long id);
    HashMap<Long, String> corregirCrucigrama(LinkedHashMap<Long,String> respuestas, Long crucigramaId);
    EvaluacionActividadAbiertaResponse corregirActividadAbierta(EvaluacionActividadAbiertaRequest request);}
