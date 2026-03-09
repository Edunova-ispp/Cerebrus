package com.cerebrus.actividad;

import java.util.List;

public interface GeneralService {
	General crearActGeneral(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible);
	General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
	General readActividad(Long id);
	GeneralTestDTO readTipoTest(Long id);
	GeneralTestMaestroDTO readTipoTestMaestro(Long id);
	General updateActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Long temaId);
	General updateTipoTest(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId);
	void deleteActividad(Long id);
	General crearGeneralClasificacion(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible);
	GeneralClasificacionMaestroDTO readTipoClasificacionMaestro(Long id);
	GeneralClasificacionDTO readTipoClasificacion(Long id);
	GeneralClasificacionMaestroDTO updateTipoClasificacion(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) ;
}
