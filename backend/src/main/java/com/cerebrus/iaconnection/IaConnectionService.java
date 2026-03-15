package com.cerebrus.iaconnection;

import com.cerebrus.comun.enumerados.TipoAct;

public interface IaConnectionService {
    
    String generarMockActividad(TipoAct tipoActividad, String prompt);
    String generarActividad(TipoAct tipoActividad, String prompt);

}