package com.cerebrus.actividad;

import java.util.List;

public interface GeneralService {
	General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, List<Long> preguntasId);
}
