package com.cerebrus.respuestaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroController;
import com.cerebrus.respuestaMaestro.RespuestaMaestroService;

@ExtendWith(MockitoExtension.class)
public class RespuestaControllerTest {

	@Mock
	private RespuestaMaestroService respuestaService;

	@InjectMocks
	private RespuestaMaestroController controller;

    // Test para verificar que el método crearRespuestaMaestro devuelve CREATED y delega correctamente al servicio
	@Test
	void crearRespuestaMaestro_devuelveCreated() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(10L);

		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Opción A");
		request.setImagen("img.png");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		RespuestaMaestro created = new RespuestaMaestro();
		created.setId(99L);
		created.setRespuesta("Opción A");
		created.setImagen("img.png");
		created.setCorrecta(true);
		created.setPregunta(pregunta);

		when(respuestaService.crearRespuestaMaestro(eq("Opción A"), eq("img.png"), eq(true), eq(10L))).thenReturn(created);

		ResponseEntity<Long> response = controller.crearRespuestaMaestro(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(99L);
		verify(respuestaService).crearRespuestaMaestro("Opción A", "img.png", true, 10L);
	}

    // Test para verificar que el método crearRespuestaMaestro lanza NullPointerException y no llama al servicio 
    // cuando se le pasa null
	@Test
	void crearRespuestaMaestro_cuandoEsNula_devuelveNullPointer() {
		assertThatThrownBy(() -> controller.crearRespuestaMaestro(null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método crearRespuestaMaestro permitte null en el campo imagen y delega correctamente al servicio
	@Test
	void crearRespuestaMaestro_permiteImagenNull() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(7L);

		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Texto");
		request.setImagen(null);
		request.setCorrecta(false);
		request.setPregunta(pregunta);

		RespuestaMaestro created = new RespuestaMaestro();
		created.setId(1L);

		when(respuestaService.crearRespuestaMaestro(eq("Texto"), eq(null), eq(false), eq(7L))).thenReturn(created);

		ResponseEntity<Long> response = controller.crearRespuestaMaestro(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(1L);
		verify(respuestaService).crearRespuestaMaestro("Texto", null, false, 7L);
	}

    // Test para verificar que el método crearRespuestaMaestro permitte null en el campo correcta y delega correctamente al servicio
	@Test
	void crearRespuestaMaestro_permiteCorrectaNull() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(8L);

		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Texto");
		request.setImagen("img");
		request.setCorrecta(null);
		request.setPregunta(pregunta);

		RespuestaMaestro created = new RespuestaMaestro();
		created.setId(2L);

		when(respuestaService.crearRespuestaMaestro(eq("Texto"), eq("img"), isNull(), eq(8L))).thenReturn(created);

		ResponseEntity<Long> response = controller.crearRespuestaMaestro(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(2L);
		verify(respuestaService).crearRespuestaMaestro("Texto", "img", null, 8L);
	}

    // Test para verificar que el método crearRespuestaMaestro lanza NullPointerException y no llama al servicio 
    // cuando se le pasa una pregunta nula
	@Test
	void crearRespuestaMaestro_cuandoPreguntaEsNull_lanzaNullPointer() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Texto");
		request.setImagen(null);
		request.setCorrecta(true);
		request.setPregunta(null);

		assertThatThrownBy(() -> controller.crearRespuestaMaestro(request))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método crearRespuestaMaestro propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void crearRespuestaMaestro_propagaNotFoundFromService() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(123L);

		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Texto");
		request.setImagen("x");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		when(respuestaService.crearRespuestaMaestro(eq("Texto"), eq("x"), eq(true), eq(123L)))
				.thenThrow(new ResourceNotFoundException("Pregunta", "id", 123L));

		assertThatThrownBy(() -> controller.crearRespuestaMaestro(request))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).crearRespuestaMaestro("Texto", "x", true, 123L);
	}

    // Test para verificar que el método crearRespuestaMaestro propaga AccessDeniedException lanzada por el servicio
	@Test
	void crearRespuestaMaestro_propagaAccessDeniedFromService() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(123L);

		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Texto");
		request.setImagen("x");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		when(respuestaService.crearRespuestaMaestro(eq("Texto"), eq("x"), eq(true), eq(123L)))
				.thenThrow(new AccessDeniedException("Solo maestros"));

		assertThatThrownBy(() -> controller.crearRespuestaMaestro(request))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).crearRespuestaMaestro("Texto", "x", true, 123L);
	}

    // Test para verificar que el método encontrarRespuestaMaestroPorId devuelve OK y delega correctamente al servicio
	@Test
	void encontrarRespuestaMaestroPorId_devuelveOk() {
		RespuestaMaestro respuesta = new RespuestaMaestro();
		respuesta.setId(5L);

		when(respuestaService.encontrarRespuestaMaestroPorId(5L)).thenReturn(respuesta);

		ResponseEntity<RespuestaMaestro> response = controller.encontrarRespuestaMaestroPorId(5L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(respuesta);
		verify(respuestaService).encontrarRespuestaMaestroPorId(5L);
	}

    // Test para verificar que el método encontrarRespuestaMaestroPorId propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void encontrarRespuestaMaestroPorId_propagaNotFoundFromService() {
		when(respuestaService.encontrarRespuestaMaestroPorId(404L))
				.thenThrow(new ResourceNotFoundException("Respuesta", "id", 404L));

		assertThatThrownBy(() -> controller.encontrarRespuestaMaestroPorId(404L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

    // Test para verificar que el método actualizarRespuestaMaestro devuelve NO_CONTENT y delega correctamente al servicio
	@Test
	void actualizarRespuestaMaestro_devuelveOk() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		ResponseEntity<Void> response = controller.actualizarRespuestaMaestro(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respuestaService).actualizarRespuestaMaestro(9L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método actualizarRespuestaMaestro usa el id del path incluso si el body tiene un 
    // id diferente, y delega correctamente al servicio
	@Test
	void actualizarRespuestaMaestro_usaPathId_inclusoSiBodyTieneDiferenteId() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setId(1234L);
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		ResponseEntity<Void> response = controller.actualizarRespuestaMaestro(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respuestaService).actualizarRespuestaMaestro(9L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método actualizarRespuestaMaestro lanza NullPointerException y no llama al servicio 
    // cuando se le pasa una request nula
	@Test
	void actualizarRespuestaMaestro_cuandoRequestEsNull_lanzaNullPointer() {
		assertThatThrownBy(() -> controller.actualizarRespuestaMaestro(1L, null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método actualizarRespuestaMaestro permitte null en el campo imagen y 
    // delega correctamente al servicio
	@Test
	void actualizarRespuestaMaestro_permiteImagenNull() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Nueva");
		request.setImagen(null);
		request.setCorrecta(true);

		ResponseEntity<Void> response = controller.actualizarRespuestaMaestro(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respuestaService).actualizarRespuestaMaestro(9L, "Nueva", null, true);
	}

    // Test para verificar que el método actualizarRespuestaMaestro propaga AccessDeniedException lanzada por el servicio
	void actualizarRespuestaMaestro_propagaAccessDeniedFromService() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Nueva");
		request.setImagen(null);
		request.setCorrecta(true);

		when(respuestaService.actualizarRespuestaMaestro(eq(1L), eq("Nueva"), eq(null), eq(true)))
				.thenThrow(new AccessDeniedException("Solo maestros"));

		assertThatThrownBy(() -> controller.actualizarRespuestaMaestro(1L, request))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).actualizarRespuestaMaestro(1L, "Nueva", null, true);
	}

    // Test para verificar que el método actualizarRespuestaMaestro propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void actualizarRespuestaMaestro_propagaNotFoundFromService() {
		RespuestaMaestro request = new RespuestaMaestro();
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		when(respuestaService.actualizarRespuestaMaestro(eq(404L), eq("Nueva"), eq("img2.png"), eq(false)))
				.thenThrow(new ResourceNotFoundException("Respuesta", "id", 404L));

		assertThatThrownBy(() -> controller.actualizarRespuestaMaestro(404L, request))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).actualizarRespuestaMaestro(404L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método eliminarRespuestaMaestroPorId devuelve NO_CONTENT y delega correctamente al servicio
	@Test
	void eliminarRespuestaMaestroPorId_devuelveNoContent() {
		doNothing().when(respuestaService).eliminarRespuestaMaestroPorId(3L);

		ResponseEntity<Void> response = controller.eliminarRespuestaMaestroPorId(3L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respuestaService).eliminarRespuestaMaestroPorId(3L);
	}

    // Test para verificar que el método eliminarRespuestaMaestroPorId propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void eliminarRespuestaMaestroPorId_propagaNotFoundFromService() {
		doThrow(new ResourceNotFoundException("Respuesta", "id", 3L))
				.when(respuestaService)
				.eliminarRespuestaMaestroPorId(3L);

		assertThatThrownBy(() -> controller.eliminarRespuestaMaestroPorId(3L))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).eliminarRespuestaMaestroPorId(3L);
	}

    // Test para verificar que el método eliminarRespuestaMaestroPorId propaga AccessDeniedException lanzada por el servicio
	@Test
	void eliminarRespuestaMaestroPorId_propagaAccessDeniedFromService() {
		doThrow(new AccessDeniedException("Solo maestros"))
				.when(respuestaService)
				.eliminarRespuestaMaestroPorId(55L);

		assertThatThrownBy(() -> controller.eliminarRespuestaMaestroPorId(55L))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).eliminarRespuestaMaestroPorId(55L);
	}
}
