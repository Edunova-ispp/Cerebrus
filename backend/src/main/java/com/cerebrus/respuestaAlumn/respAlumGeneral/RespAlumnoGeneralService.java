package com.cerebrus.respuestaAlumn.respAlumGeneral;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaRequest;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralResumenDTO;

public interface RespAlumnoGeneralService {
    RespAlumnoGeneralCreateResponse crearRespuestaAlumnoGeneral(Long actAlumnoId, Long respuestaId, Long preguntaId);
    RespAlumnoGeneral encontrarRespuestaAlumnoGeneralPorId(Long id);
    RespAlumnoGeneral actualizarRespuestaAlumnoGeneral(Long id, Boolean correcta, Long actAlumnoId, String respuesta, Long preguntaId);
    void eliminarRespuestaAlumnoGeneralPorId(Long id);
    Boolean corregirRespuestaAlumnoGeneral(Long id);
    boolean corregirRespuestaAlumnoGeneralClasificacion(Long id);
    List<RespAlumnoGeneralResumenDTO> listarRespuestasPorActividadAlumno(Long actividadAlumnoId);
    HashMap<Long, String> corregirCrucigrama(LinkedHashMap<Long,String> respuestas, Long crucigramaId);
    EvaluacionActividadAbiertaResponse corregirActividadAbierta(EvaluacionActividadAbiertaRequest request);}
