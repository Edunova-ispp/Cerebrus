package com.cerebrus.iaconnection;

import com.cerebrus.TipoAct;

public interface IaConnectionService {
    
    String generarMockActividad(TipoAct tipoActividad, String prompt);

}