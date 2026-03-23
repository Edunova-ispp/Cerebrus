package com.cerebrus.actividad.general;

import java.util.List;

import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.CrucigramaRequest;
import com.cerebrus.actividad.general.dto.GeneralAbiertaAlumnoDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;

public interface GeneralService {
	General crearActGeneral(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible);
	General crearTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
	General crearTipoCarta(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
	General readActividad(Long id);
	GeneralTestDTO readTipoTest(Long id);
	GeneralTestMaestroDTO readTipoTestMaestro(Long id);
	GeneralCartaDTO readTipoCarta(Long id);
	GeneralCartaMaestroDTO readTipoCartaMaestro(Long id);
	General updateActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Long temaId);
	General updateTipoTest(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId);
	General updateTipoCarta(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId);
	void deleteActividad(Long id);
	General crearGeneralClasificacion(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible);
	GeneralClasificacionMaestroDTO readTipoClasificacionMaestro(Long id);
	GeneralClasificacionDTO readTipoClasificacion(Long id);
	GeneralClasificacionMaestroDTO updateTipoClasificacion(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) ;
    CrucigramaDTO crearTipoCrucigrama(CrucigramaRequest crucigrama);
    CrucigramaDTO readTipoCrucigrama(Long id);
    CrucigramaDTO updateTipoCrucigrama(Long id, CrucigramaRequest crucigrama);
    General crearTipoAbierta(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
    GeneralAbiertaAlumnoDTO readTipoAbierta(Long id);
    GeneralAbiertaMaestroDTO readTipoAbiertaMaestro(Long id);
    General updateTipoAbierta(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId);
}
