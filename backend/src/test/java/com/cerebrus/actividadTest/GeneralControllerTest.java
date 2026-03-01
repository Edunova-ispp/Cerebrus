package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.actividad.General;
import com.cerebrus.actividad.GeneralController;
import com.cerebrus.actividad.GeneralService;
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

		when(generalService.crearActGeneral("Titulo", "Desc", 10, 5L, false, "coment"))
				.thenReturn(creada);

		ResponseEntity<General> response = generalController.crearActGeneral(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(creada);
		verify(generalService).crearActGeneral("Titulo", "Desc", 10, 5L, false, "coment");
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

		when(generalService.crearActGeneral("Titulo", null, 10, 5L, true, null))
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
	void crearTipoTest_requestValido_mapeaPreguntasId_yDevuelve201() {
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

		when(generalService.crearTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(true), eq("c"), any()))
				.thenReturn(creada);

		ResponseEntity<General> response = generalController.crearTipoTest(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(creada);

		verify(generalService).crearTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(true), eq("c"),
				preguntasIdCaptor.capture());
		assertThat(preguntasIdCaptor.getValue()).containsExactly(1L, 2L);
	}

    // Tests para verificar que si se crea un tipo test con preguntas vacías, se pasa una lista vacía al servicio, 
    // y se devuelve 201
	@Test
	void crearTipoTest_preguntasVacias_pasaListaVacia() {
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

		when(generalService.crearTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(false), eq(null), any()))
				.thenReturn(new General());

		generalController.crearTipoTest(request);

		verify(generalService).crearTipoTest(eq("T"), eq("D"), eq(5), eq(10L), eq(false), eq(null),
				preguntasIdCaptor.capture());
		assertThat(preguntasIdCaptor.getValue()).isEmpty();
	}

    // Tests para verificar que se lanza NullPointerException si preguntas es null al crear un tipo test, 
    // y no se llama al servicio
	@Test
	void crearTipoTest_preguntasNull_lanzaNullPointer() {
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

		assertThatThrownBy(() -> generalController.crearTipoTest(request))
				.isInstanceOf(NullPointerException.class);

		verify(generalService, never()).crearTipoTest(any(), any(), any(), any(), any(), any(), any());
	}

    // Tests para verificar que se devuelve 200 con la actividad actualizada si el request es válido al actualizar una 
    // actividad general
	@Test
	void readActividad_ok_devuelve200_yBody() {
		General general = new General();
		general.setId(7L);

		when(generalService.readActividad(7L)).thenReturn(general);

		ResponseEntity<General> response = generalController.readActividad(7L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(general);
		verify(generalService).readActividad(7L);
	}

    // Tests para verificar que se crea una actividad general con un request válido, y que se llama al servicio con 
    // los campos correctos
	@Test
	void updateActGeneral_requestValido_devuelve200() {
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

		General actualizado = new General();
		actualizado.setId(12L);

		when(generalService.updateActGeneral(12L, "Nuevo", "Desc", 10, false, "c", 3, 4, 5L))
				.thenReturn(actualizado);

		ResponseEntity<General> response = generalController.updateActGeneral(12L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(actualizado);
		verify(generalService).updateActGeneral(12L, "Nuevo", "Desc", 10, false, "c", 3, 4, 5L);
	}

    // Tests para verificar que se actualiza una actividad tipo test con un request válido, y que se llama al servicio 
    // con los campos correctos
	@Test
	void updateTipoTest_requestValido_mapeaPreguntasId_yDevuelve200() {
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

		General actualizado = new General();
		actualizado.setId(12L);

		when(generalService.updateTipoTest(eq(12L), eq("Nuevo"), eq("Desc"), eq(10), eq(true), eq("c"), any(), eq(3), eq(4), eq(5L)))
				.thenReturn(actualizado);

		ResponseEntity<General> response = generalController.updateTipoTest(12L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(actualizado);

		verify(generalService).updateTipoTest(eq(12L), eq("Nuevo"), eq("Desc"), eq(10), eq(true), eq("c"),
				preguntasIdCaptor.capture(), eq(3), eq(4), eq(5L));
		assertThat(preguntasIdCaptor.getValue()).containsExactly(1L);
	}

    // Tests para verificar que al borrar una actividad tipo test, se devuelve 204 y se llama al servicio con el id 
    // correcto
	@Test
	void deleteActividad_ok_devuelve204() {
		ResponseEntity<Void> response = generalController.deleteActividad(77L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(generalService).deleteActividad(77L);
	}
}
