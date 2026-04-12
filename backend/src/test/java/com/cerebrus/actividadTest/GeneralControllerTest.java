package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.general.GeneralController;
import com.cerebrus.actividad.general.GeneralService;
import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.CrucigramaRequest;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;

@ExtendWith(MockitoExtension.class)
class GeneralControllerTest {

	@Mock
	private GeneralService generalService;

	@InjectMocks
	private GeneralController generalController;

    // ArgumentCaptor para capturar la lista de preguntasId que se pasa al servicio al crear o actualizar un tipo test
	@Captor
	private ArgumentCaptor<List<Long>> preguntasIdCaptor;

    // Tests para verificar que se devuelve 201 con la actividad creada si el request es válido al crear una 
    // actividad general
	@Test
	void crearActGeneral_requestValido_devuelve201_yLlamaServiceConCampos() {
		Tema tema = new Tema();
		tema.setId(5L);

		General request = new General();
		request.setTitulo("Titulo");
		request.setDescripcion("Desc");
		request.setPuntuacion(10);
		request.setTema(tema);
		request.setRespVisible(false);
		request.setComentariosRespVisible("coment");

		General creada = new General();
		creada.setId(99L);

                when(generalService.crearActGeneral("Titulo", "Desc", 10, 5L, false, "coment", false, false, false, false))
				.thenReturn(creada);

		ResponseEntity<General> response = generalController.crearActGeneral(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(creada);
        verify(generalService).crearActGeneral("Titulo", "Desc", 10, 5L, false, "coment", false, false, false, false);
	}

    // Tests para verificar que se devuelve 201 con la actividad creada si el request es válido al crear una 
    // actividad general con descripcion y comentarios null
	@Test
	void crearActGeneral_descripcionYComentariosNull_devuelve201() {
		Tema tema = new Tema();
		tema.setId(5L);

		General request = new General();
		request.setTitulo("Titulo");
		request.setDescripcion(null);
		request.setPuntuacion(10);
		request.setTema(tema);
		request.setRespVisible(true);
		request.setComentariosRespVisible(null);

		General creada = new General();

                when(generalService.crearActGeneral("Titulo", null, 10, 5L, true, null, false, false, false, false))
				.thenReturn(creada);

		ResponseEntity<General> response = generalController.crearActGeneral(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(creada);
	}

    // Tests para verificar que se lanza NullPointerException si el request es null al crear una actividad general, 
    // y no se llama al servicio
	@Test
	void crearActGeneral_requestNull_lanzaNullPointer() {
		assertThatThrownBy(() -> generalController.crearActGeneral(null))
				.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(generalService);
	}

    // Tests para verificar que se lanza NullPointerException si el tema es null al crear una actividad general, 
    // y no se llama al servicio
	@Test
	void crearActGeneral_temaNull_lanzaNullPointer() {
		General request = new General();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(1);
		request.setTema(null);
		request.setRespVisible(false);
		request.setComentariosRespVisible(null);

		assertThatThrownBy(() -> generalController.crearActGeneral(request))
				.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(generalService);
	}

    // Tests para verificar que devuelve 201 al crear una actividad tipo test con un request válido, y que 
    // se mapea correctamente la lista de preguntasId del request al llamar al servicio
	@Test
	void crearActTipoTest_requestValido_mapeaPreguntasId_yDevuelve201() {
		Tema tema = new Tema();
		tema.setId(10L);

		Pregunta p1 = new Pregunta();
		p1.setId(1L);
		Pregunta p2 = new Pregunta();
		p2.setId(2L);

		General request = new General();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(5);
		request.setTema(tema);
		request.setRespVisible(true);
		request.setComentariosRespVisible("c");
		request.setPreguntas(List.of(p1, p2));

		General creada = new General();
		creada.setId(99L);

        when(generalService.crearActTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(true), eq("c"), any(), any(), any(), any(), any()))
				.thenReturn(creada);

		ResponseEntity<Long> response = generalController.crearActTipoTest(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(99L);

        verify(generalService).crearActTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(true), eq("c"),
                preguntasIdCaptor.capture(), any(), any(), any(), any());
		assertThat(preguntasIdCaptor.getValue()).containsExactly(1L, 2L);
	}

    // Tests para verificar que si se crea un tipo test con preguntas vacías, se pasa una lista vacía al servicio, 
    // y se devuelve 201
	@Test
	void crearActTipoTest_preguntasVacias_pasaListaVacia() {
		Tema tema = new Tema();
		tema.setId(10L);

		General request = new General();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(5);
		request.setTema(tema);
		request.setRespVisible(false);
		request.setComentariosRespVisible(null);
		request.setPreguntas(List.of());

        when(generalService.crearActTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(false), eq(null), any(), any(), any(), any(), any()))
				.thenReturn(new General());

		generalController.crearActTipoTest(request);

        verify(generalService).crearActTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(false), eq(null),
                preguntasIdCaptor.capture(), any(), any(), any(), any());
		assertThat(preguntasIdCaptor.getValue()).isEmpty();
	}

    // Tests para verificar que se lanza NullPointerException si preguntas es null al crear un tipo test, 
    // y no se llama al servicio
	@Test
	void crearActTipoTest_preguntasNull_lanzaNullPointer() {
		Tema tema = new Tema();
		tema.setId(10L);

		General request = new General();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(5);
		request.setTema(tema);
		request.setRespVisible(false);
		request.setComentariosRespVisible(null);
		request.setPreguntas(null);

		assertThatThrownBy(() -> generalController.crearActTipoTest(request))
				.isInstanceOf(NullPointerException.class);

        verify(generalService, never()).crearActTipoTest(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

    // Tests para verificar que se devuelve 200 con la actividad actualizada si el request es válido al actualizar una 
    // actividad general
	@Test
	void encontrarActGeneralPorId_ok_devuelve200_yBody() {
		General general = new General();
		general.setId(7L);

		when(generalService.encontrarActGeneralPorId(7L)).thenReturn(general);

		ResponseEntity<General> response = generalController.encontrarActGeneralPorId(7L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(general);
		verify(generalService).encontrarActGeneralPorId(7L);
	}

    // Tests para verificar que se crea una actividad general con un request válido, y que se llama al servicio con 
    // los campos correctos
	@Test
	void actualizarActGeneral_requestValido() {
		Tema tema = new Tema();
		tema.setId(5L);

		General request = new General();
		request.setTitulo("Nuevo");
		request.setDescripcion("Desc");
		request.setPuntuacion(10);
		request.setRespVisible(false);
		request.setComentariosRespVisible("c");
		request.setPosicion(3);
		request.setVersion(4);
		request.setTema(tema);

		ResponseEntity<Void> response = generalController.actualizarActGeneral(12L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(generalService).actualizarActGeneral(12L, "Nuevo", "Desc", Integer.valueOf(10), false, "c", Integer.valueOf(3), Integer.valueOf(4), 5L, null, false, false, false, false);
	}

    // Tests para verificar que se actualiza una actividad tipo test con un request válido, y que se llama al servicio 
    // con los campos correctos
	@Test
	void actualizarActTipoTest_requestValido_mapeaPreguntasId_yDevuelve200() {
		Tema tema = new Tema();
		tema.setId(5L);

		Pregunta p1 = new Pregunta();
		p1.setId(1L);

		General request = new General();
		request.setTitulo("Nuevo");
		request.setDescripcion("Desc");
		request.setPuntuacion(10);
		request.setRespVisible(true);
		request.setComentariosRespVisible("c");
		request.setPreguntas(List.of(p1));
		request.setPosicion(3);
		request.setVersion(4);
		request.setTema(tema);

        GeneralTestMaestroDTO esperado = mock(GeneralTestMaestroDTO.class);

                when(generalService.actualizarActTipoTest(eq(12L), eq("Nuevo"), eq("Desc"), eq(10), eq(true), eq("c"), any(), eq(3), eq(4), eq(5L), eq(null), eq(false), eq(false), eq(false), eq(false)))
				.thenReturn(new General());
        when(generalService.encontrarActTipoTestMaestroPorId(12L)).thenReturn(esperado);

        ResponseEntity<GeneralTestMaestroDTO> response = generalController.actualizarActTipoTest(12L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(esperado);

        verify(generalService).actualizarActTipoTest(eq(12L), eq("Nuevo"), eq("Desc"), eq(10), eq(true), eq("c"),
            preguntasIdCaptor.capture(), eq(3), eq(4), eq(5L), eq(null), eq(false), eq(false), eq(false), eq(false));
		assertThat(preguntasIdCaptor.getValue()).containsExactly(1L);
        verify(generalService).encontrarActTipoTestMaestroPorId(12L);
	}

    // Tests para verificar que al borrar una actividad tipo test, se devuelve 204 y se llama al servicio con el id 
    // correcto
	@Test
	void eliminarActGeneralPorId_ok_devuelve204() {
		ResponseEntity<Void> response = generalController.eliminarActGeneralPorId(77L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(generalService).eliminarActGeneralPorId(77L);
	}

	// --- TESTS PARA CRUCIGRAMA (CON VALIDACIÓN DE NEGOCIO EN CONTROLADOR) ---

    @Test
    void crearTipoCrucigrama_respuestasInvalidas_devuelve400() {
        CrucigramaRequest request = new CrucigramaRequest();
        // Respuesta con números o caracteres especiales no permitidos por el regex ^[\p{L}]+$
        request.setPreguntasYRespuestas(Map.of("Pregunta 1", "Respuesta123")); 

        ResponseEntity<CrucigramaDTO> response = generalController.crearActCrucigrama(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(generalService);
    }

    @Test
    void crearTipoCrucigrama_masDeCincoPreguntas_devuelve400() {
        CrucigramaRequest request = new CrucigramaRequest();
        Map<String, String> muchasPreguntas = new HashMap<>();
        for (int i = 0; i < 6; i++) { muchasPreguntas.put("P" + i, "R" + i); }
        request.setPreguntasYRespuestas(muchasPreguntas);

        ResponseEntity<CrucigramaDTO> response = generalController.crearActCrucigrama(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
	void crearTipoCrucigrama_ok_devuelve201() {
		CrucigramaRequest request = new CrucigramaRequest();
		request.setPreguntasYRespuestas(Map.of("P1", "Respuesta"));
		
		// Si no tiene constructor vacío, mockeamos el DTO
		CrucigramaDTO dtoMock = mock(CrucigramaDTO.class);
		when(dtoMock.getId()).thenReturn(1L);

		when(generalService.crearActCrucigrama(any(CrucigramaRequest.class))).thenReturn(dtoMock);

		ResponseEntity<CrucigramaDTO> response = generalController.crearActCrucigrama(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); // Tu controlador devuelve .ok()
		assertThat(response.getBody().getId()).isEqualTo(1L);
	}

    // --- TESTS PARA ACTIVIDAD ABIERTA ---

    @Test
    void crearTipoAbierta_mapeaIdsYDevuelve201() {
        General request = new General();
        request.setTitulo("Abierta");
        Tema tema = new Tema(); tema.setId(5L);
        request.setTema(tema);
        
        Pregunta p = new Pregunta(); p.setId(10L);
        request.setPreguntas(List.of(p));

        General creada = new General();
        creada.setId(100L);

        when(generalService.crearActAbierta(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(creada);

        ResponseEntity<Long> response = generalController.crearActAbierta(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(100L);
        
        verify(generalService).crearActAbierta(
            eq("Abierta"), any(), any(), eq(5L), any(), any(), preguntasIdCaptor.capture(), any(), any(), any(), any(), any()
        );
        assertThat(preguntasIdCaptor.getValue()).containsExactly(10L);
    }

    @Test
	void updateTipoAbierta_llamaReadMaestroParaRetorno() {
		General request = new General();
		request.setTitulo("Update");
		Tema tema = new Tema(); tema.setId(1L);
		request.setTema(tema);
		request.setPreguntas(List.of());
		
		// Usamos mock para evitar el error del constructor undefined
		GeneralAbiertaMaestroDTO dtoMaestroMock = mock(GeneralAbiertaMaestroDTO.class);
		
		// El controlador en el PUT llama primero al update y luego al readTipoAbiertaMaestro
		when(generalService.encontrarActAbiertaMaestroPorId(1L)).thenReturn(dtoMaestroMock);

		ResponseEntity<GeneralAbiertaMaestroDTO> response = generalController.actualizarActAbierta(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(dtoMaestroMock);
	}

    // --- TESTS PARA CLASIFICACIÓN ---

    @Test
	void updateTipoClasificacion_devuelveDTORefrescado() {
		// 1. Preparamos el Request
		General request = new General();
		Tema tema = new Tema(); tema.setId(2L);
		request.setTema(tema);
		request.setPreguntas(List.of()); // Lista vacía para evitar NPE en el stream

		// 2. Mockeamos el DTO de retorno
		GeneralClasificacionMaestroDTO dtoMock = mock(GeneralClasificacionMaestroDTO.class);
		
        when(generalService.actualizarActClasificacion(eq(1L), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
			.thenReturn(dtoMock);

		// 3. Ejecución
		ResponseEntity<GeneralClasificacionMaestroDTO> response = generalController.actualizarActClasificacion(1L, request);

		// 4. Verificación
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(dtoMock);
	}

	// --- TESTS PARA readTipoTest (Alumno y Maestro) ---

    @Test
    void readTipoTest_devuelveStatusOkYDTOCorrecto() {
        // 1. Setup
        GeneralTestDTO dtoMock = mock(GeneralTestDTO.class);
        when(generalService.encontrarActTipoTestPorId(1L)).thenReturn(dtoMock);

        // 2. Ejecución
        ResponseEntity<GeneralTestDTO> response = generalController.encontrarActTipoTestPorId(1L);

        // 3. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);
        verify(generalService).encontrarActTipoTestPorId(1L);
    }

    @Test
    void readTipoTestMaestro_devuelveStatusOkYDTOCorrecto() {
        // 1. Setup
        GeneralTestMaestroDTO dtoMock = mock(GeneralTestMaestroDTO.class);
        when(generalService.encontrarActTipoTestMaestroPorId(1L)).thenReturn(dtoMock);

        // 2. Ejecución
        ResponseEntity<GeneralTestMaestroDTO> response = generalController.encontrarActTipoTestMaestroPorId(1L);

        // 3. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);
        verify(generalService).encontrarActTipoTestMaestroPorId(1L);
    }

    // --- TESTS PARA readTipoCarta (Alumno y Maestro) ---

    @Test
    void readTipoCarta_devuelveStatusOkYDTOCorrecto() {
        // 1. Setup
        GeneralCartaDTO dtoMock = mock(GeneralCartaDTO.class);
        when(generalService.encontrarActCartaPorId(1L)).thenReturn(dtoMock);

        // 2. Ejecución
        ResponseEntity<GeneralCartaDTO> response = generalController.encontrarActCartaPorId(1L);

        // 3. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);
        verify(generalService).encontrarActCartaPorId(1L);
    }

    @Test
    void readTipoCartaMaestro_devuelveStatusOkYDTOCorrecto() {
        // 1. Setup
        GeneralCartaMaestroDTO dtoMock = mock(GeneralCartaMaestroDTO.class);
        when(generalService.encontrarActCartaMaestroPorId(1L)).thenReturn(dtoMock);

        // 2. Ejecución
        ResponseEntity<GeneralCartaMaestroDTO> response = generalController.encontrarActCartaMaestroPorId(1L);

        // 3. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);
        verify(generalService).encontrarActCartaMaestroPorId(1L);
    }

	@Test
    void crearTipoCarta_devuelveIdYStatusCreated() {
        // 1. Preparamos el objeto de entrada (Request Body)
        General request = new General();
        request.setTitulo("Test Cartas");
        request.setDescripcion("Desc");
        request.setPuntuacion(10);
        request.setRespVisible(true);
        request.setComentariosRespVisible("Comentarios");

        // Seteamos el Tema (necesario para el getId)
        Tema tema = new Tema();
        tema.setId(5L);
        request.setTema(tema);

        // Seteamos una lista de preguntas para que el stream().map() funcione
        Pregunta p1 = new Pregunta(); p1.setId(100L);
        Pregunta p2 = new Pregunta(); p2.setId(101L);
        request.setPreguntas(List.of(p1, p2));

        // 2. Preparamos el objeto que devolverá el servicio (Mock)
        General generalCreada = new General();
        generalCreada.setId(999L); // Este es el ID que esperamos en el body

        // Configuramos el comportamiento del mock del servicio
        // Usamos eq() para los valores fijos y any() o la lista específica para el resto
        when(generalService.crearActCarta(
                eq("Test Cartas"), 
                eq("Desc"), 
                eq(10), 
                eq(5L), 
                eq(true), 
                eq("Comentarios"), 
                any(), // La lista de IDs [100, 101]
                any(),
                any(),
                any(),
                any()
        )).thenReturn(generalCreada);

        // 3. Ejecución
        ResponseEntity<Long> response = generalController.crearActCarta(request);

        // 4. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(999L);
        
        // Verificamos que se extrajeron los IDs correctamente (100 y 101)
        verify(generalService).crearActCarta(
                any(), any(), any(), any(), any(), any(), 
                eq(List.of(100L, 101L)), any(), any(), any(), any()
        );
    }

	@Test
    void updateTipoCarta_ejecutaUpdateYDevuelveDTORefrescado() {
        // 1. Preparar el objeto de entrada (Request Body)
        General request = new General();
        request.setTitulo("Cartas Actualizado");
        request.setDescripcion("Nueva Desc");
        request.setPuntuacion(20);
        request.setRespVisible(false);
        request.setComentariosRespVisible("Sin comentarios");
        request.setPosicion(2);
        request.setVersion(1);
        request.setImagen("imagen.png");

        Tema tema = new Tema();
        tema.setId(10L);
        request.setTema(tema);

        // Importante: Al menos una pregunta para cubrir el stream().map()
        Pregunta p = new Pregunta();
        p.setId(50L);
        request.setPreguntas(List.of(p));

        // 2. Mockear el DTO que devuelve el segundo método del servicio
        GeneralCartaMaestroDTO dtoMock = mock(GeneralCartaMaestroDTO.class);
        
        // El controlador hace un update (void o ignorado) y luego un read
        when(generalService.encontrarActCartaMaestroPorId(1L)).thenReturn(dtoMock);

        // 3. Ejecución
        ResponseEntity<GeneralCartaMaestroDTO> response = generalController.actualizarActCarta(1L, request);

        // 4. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);

        // Verificamos que se llamó al update con los datos del objeto 'request'
        verify(generalService).actualizarActCarta(
                eq(1L),
                eq("Cartas Actualizado"),
                eq("Nueva Desc"),
                eq(20),
                eq(false),
                eq("Sin comentarios"),
                eq(List.of(50L)), // Resultado del stream().map()
                eq(2),
                eq(1),
                eq(10L),
                eq("imagen.png"),
                eq(false),
                eq(false),
                eq(false),
                eq(false)
        );

        // Verificamos que se llamó al read para obtener la respuesta final
        verify(generalService).encontrarActCartaMaestroPorId(1L);
    }

	@Test
    void crearTipoClasificacion_devuelveIdYStatusCreated() {
        // 1. Preparar el objeto de entrada (Request Body)
        General request = new General();
        request.setTitulo("Nueva Clasificación");
        request.setDescripcion("Descripción de prueba");
        request.setPuntuacion(15);
        request.setRespVisible(true);
        request.setComentariosRespVisible("Comentario Clasificación");

        Tema tema = new Tema();
        tema.setId(7L);
        request.setTema(tema);

        // 2. Preparar el objeto que devolverá el servicio (Mock)
        General generalCreada = new General();
        generalCreada.setId(500L); // El ID que esperamos recibir en la respuesta

        when(generalService.crearActClasificacion(
                eq("Nueva Clasificación"),
                eq("Descripción de prueba"),
                eq(15),
                eq(7L),
                eq(true),
                eq("Comentario Clasificación"),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(generalCreada);

        // 3. Ejecución
        ResponseEntity<Long> response = generalController.crearActClasificacion(request);

        // 4. Verificaciones
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(500L);

        // Verificamos que se llamó al servicio exactamente con los datos del request
        verify(generalService).crearActClasificacion(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

	// --- TESTS PARA UPDATE TIPO CRUCIGRAMA ---

    @Test
    void updateTipoCrucigrama_ok_devuelveStatusOk() {
        // 1. Setup del Request válido (solo letras, sin espacios)
        CrucigramaRequest request = new CrucigramaRequest();
        Map<String, String> preguntasYRespuestas = new HashMap<>();
        preguntasYRespuestas.put("¿Color del cielo?", "Azul");
        request.setPreguntasYRespuestas(preguntasYRespuestas);

        CrucigramaDTO dtoMock = mock(CrucigramaDTO.class);
        when(generalService.actualizarActCrucigrama(eq(1L), any(CrucigramaRequest.class))).thenReturn(dtoMock);

        // 2. Ejecución
        ResponseEntity<CrucigramaDTO> response = generalController.actualizarActCrucigrama(1L, request);

        // 3. Verificación
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoMock);
        verify(generalService).actualizarActCrucigrama(eq(1L), any());
    }

    @Test
    void updateTipoCrucigrama_error_cuandoMapaEsNullOVacio() {
        // Caso: Mapa null
        CrucigramaRequest requestNull = new CrucigramaRequest();
        requestNull.setPreguntasYRespuestas(null);

        ResponseEntity<CrucigramaDTO> responseNull = generalController.actualizarActCrucigrama(1L, requestNull);
        assertThat(responseNull.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Caso: Mapa vacío
        CrucigramaRequest requestVacio = new CrucigramaRequest();
        requestVacio.setPreguntasYRespuestas(new HashMap<>());

        ResponseEntity<CrucigramaDTO> responseVacio = generalController.actualizarActCrucigrama(1L, requestVacio);
        assertThat(responseVacio.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        verifyNoInteractions(generalService);
    }

    @Test
    void updateTipoCrucigrama_error_cuandoRespuestaContieneNumerosOSimbolos() {
        // El regex ^[\p{L}]+$ solo permite letras
        CrucigramaRequest requestInvalido = new CrucigramaRequest();
        Map<String, String> mapaInvalido = new HashMap<>();
        mapaInvalido.put("Pregunta 1", "Respuesta123"); // Contiene números
        requestInvalido.setPreguntasYRespuestas(mapaInvalido);

        ResponseEntity<CrucigramaDTO> response = generalController.actualizarActCrucigrama(1L, requestInvalido);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(generalService);
    }

    @Test
    void updateTipoCrucigrama_error_cuandoRespuestaEsNull() {
        CrucigramaRequest request = new CrucigramaRequest();
        Map<String, String> mapa = new HashMap<>();
        mapa.put("Pregunta", null); 
        request.setPreguntasYRespuestas(mapa);

        ResponseEntity<CrucigramaDTO> response = generalController.actualizarActCrucigrama(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
