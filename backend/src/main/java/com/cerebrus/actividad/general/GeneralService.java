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
	General crearActTipoTest(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
	General crearActCarta(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId);
	General crearActClasificacion(String titulo, String descripcion, Integer puntuacion, Long temaId, 
        Boolean respVisible, String comentariosRespVisible);
	CrucigramaDTO crearActCrucigrama(CrucigramaRequest crucigrama);
	General crearActAbierta(String titulo, String descripcion, Integer puntuacion, Long temaId, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, String imagen);
	General encontrarActGeneralPorId(Long id);
	GeneralTestDTO encontrarActTipoTestPorId(Long id);
	GeneralTestMaestroDTO encontrarActTipoTestMaestroPorId(Long id);
	GeneralCartaDTO encontrarActCartaPorId(Long id);
	GeneralCartaMaestroDTO encontrarActCartaMaestroPorId(Long id);
	GeneralClasificacionDTO encontrarActClasificacionPorId(Long id);
	GeneralClasificacionMaestroDTO encontrarActClasificacionMaestroPorId(Long id);
	CrucigramaDTO encontrarActCrucigramaPorId(Long id);
	GeneralAbiertaAlumnoDTO encontrarActAbiertaPorId(Long id);
	GeneralAbiertaMaestroDTO encontrarActAbiertaMaestroPorId(Long id);
	General actualizarActGeneral(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, Integer posicion, Integer version, Long temaId, String imagen);
	General actualizarActTipoTest(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId, String imagen);
	General actualizarActCarta(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId, String imagen);
	GeneralClasificacionMaestroDTO actualizarActClasificacion(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, 
        String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId) ;
	CrucigramaDTO actualizarActCrucigrama(Long id, CrucigramaRequest crucigrama);
    General actualizarActAbierta(Long id, String titulo, String descripcion, Integer puntuacion, Boolean respVisible, String comentariosRespVisible, List<Long> preguntasId, Integer posicion, Integer version, Long temaId, String imagen);
	void eliminarActGeneralPorId(Long id);
}
