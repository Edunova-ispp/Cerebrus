package com.cerebrus.iaconnection;

import com.cerebrus.comun.enumerados.TipoAct;
import java.util.Map;

public interface IaConnectionService {
    
    String generarMockActividad(TipoAct tipoActividad, String prompt);
    String generarActividad(TipoAct tipoActividad, String prompt);
    Map<String, Object> evaluarRespuestaAbierta(String pregunta, String respuestaAlumno, String respuestaModelo, Integer puntuacionMaxima);

}