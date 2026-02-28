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
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaController;
import com.cerebrus.respuesta.RespuestaService;

@ExtendWith(MockitoExtension.class)
public class RespuestaControllerTest {

	@Mock
	private RespuestaService respuestaService;

	@InjectMocks
	private RespuestaController controller;

    // Test para verificar que el método crearRespuesta devuelve CREATED y delega correctamente al servicio
	@Test
	void crearRespuesta_devuelveCreated() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(10L);

		Respuesta request = new Respuesta();
		request.setRespuesta("Opción A");
		request.setImagen("img.png");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		Respuesta created = new Respuesta();
		created.setId(99L);
		created.setRespuesta("Opción A");
		created.setImagen("img.png");
		created.setCorrecta(true);
		created.setPregunta(pregunta);

		when(respuestaService.crearRespuesta(eq("Opción A"), eq("img.png"), eq(true), eq(10L))).thenReturn(created);

		ResponseEntity<Respuesta> response = controller.crearRespuesta(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(created);
		verify(respuestaService).crearRespuesta("Opción A", "img.png", true, 10L);
	}

    // Test para verificar que el método crearRespuesta lanza NullPointerException y no llama al servicio 
    // cuando se le pasa null
	@Test
	void crearRespuesta_cuandoEsNula_devuelveNullPointer() {
		assertThatThrownBy(() -> controller.crearRespuesta(null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método crearRespuesta permitte null en el campo imagen y delega correctamente al servicio
	@Test
	void crearRespuesta_permiteImagenNull() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(7L);

		Respuesta request = new Respuesta();
		request.setRespuesta("Texto");
		request.setImagen(null);
		request.setCorrecta(false);
		request.setPregunta(pregunta);

		Respuesta created = new Respuesta();
		created.setId(1L);

		when(respuestaService.crearRespuesta(eq("Texto"), eq(null), eq(false), eq(7L))).thenReturn(created);

		ResponseEntity<Respuesta> response = controller.crearRespuesta(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(created);
		verify(respuestaService).crearRespuesta("Texto", null, false, 7L);
	}

    // Test para verificar que el método crearRespuesta permitte null en el campo correcta y delega correctamente al servicio
	@Test
	void crearRespuesta_permiteCorrectaNull() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(8L);

		Respuesta request = new Respuesta();
		request.setRespuesta("Texto");
		request.setImagen("img");
		request.setCorrecta(null);
		request.setPregunta(pregunta);

		Respuesta created = new Respuesta();
		created.setId(2L);

		when(respuestaService.crearRespuesta(eq("Texto"), eq("img"), isNull(), eq(8L))).thenReturn(created);

		ResponseEntity<Respuesta> response = controller.crearRespuesta(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(created);
		verify(respuestaService).crearRespuesta("Texto", "img", null, 8L);
	}

    // Test para verificar que el método crearRespuesta lanza NullPointerException y no llama al servicio 
    // cuando se le pasa una pregunta nula
	@Test
	void crearRespuesta_cuandoPreguntaEsNull_lanzaNullPointer() {
		Respuesta request = new Respuesta();
		request.setRespuesta("Texto");
		request.setImagen(null);
		request.setCorrecta(true);
		request.setPregunta(null);

		assertThatThrownBy(() -> controller.crearRespuesta(request))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método crearRespuesta propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void crearRespuesta_propagaNotFoundFromService() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(123L);

		Respuesta request = new Respuesta();
		request.setRespuesta("Texto");
		request.setImagen("x");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		when(respuestaService.crearRespuesta(eq("Texto"), eq("x"), eq(true), eq(123L)))
				.thenThrow(new ResourceNotFoundException("Pregunta", "id", 123L));

		assertThatThrownBy(() -> controller.crearRespuesta(request))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).crearRespuesta("Texto", "x", true, 123L);
	}

    // Test para verificar que el método crearRespuesta propaga AccessDeniedException lanzada por el servicio
	@Test
	void crearRespuesta_propagaAccessDeniedFromService() {
		Pregunta pregunta = new Pregunta();
		pregunta.setId(123L);

		Respuesta request = new Respuesta();
		request.setRespuesta("Texto");
		request.setImagen("x");
		request.setCorrecta(true);
		request.setPregunta(pregunta);

		when(respuestaService.crearRespuesta(eq("Texto"), eq("x"), eq(true), eq(123L)))
				.thenThrow(new AccessDeniedException("Solo maestros"));

		assertThatThrownBy(() -> controller.crearRespuesta(request))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).crearRespuesta("Texto", "x", true, 123L);
	}

    // Test para verificar que el método readRespuesta devuelve OK y delega correctamente al servicio
	@Test
	void readRespuesta_devuelveOk() {
		Respuesta respuesta = new Respuesta();
		respuesta.setId(5L);

		when(respuestaService.readRespuesta(5L)).thenReturn(respuesta);

		ResponseEntity<Respuesta> response = controller.readRespuesta(5L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(respuesta);
		verify(respuestaService).readRespuesta(5L);
	}

    // Test para verificar que el método readRespuesta propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void readRespuesta_propagaNotFoundFromService() {
		when(respuestaService.readRespuesta(404L))
				.thenThrow(new ResourceNotFoundException("Respuesta", "id", 404L));

		assertThatThrownBy(() -> controller.readRespuesta(404L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

    // Test para verificar que el método updateRespuesta devuelve OK y delega correctamente al servicio
	@Test
	void updateRespuesta_devuelveOk() {
		Respuesta request = new Respuesta();
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		Respuesta updated = new Respuesta();
		updated.setId(9L);
		updated.setRespuesta("Nueva");
		updated.setImagen("img2.png");
		updated.setCorrecta(false);

		when(respuestaService.updateRespuesta(eq(9L), eq("Nueva"), eq("img2.png"), eq(false))).thenReturn(updated);

		ResponseEntity<Respuesta> response = controller.updateRespuesta(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(updated);
		verify(respuestaService).updateRespuesta(9L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método updateRespuesta usa el id del path incluso si el body tiene un 
    // id diferente, y delega correctamente al servicio
	@Test
	void updateRespuesta_usaPathId_inclusoSiBodyTieneDiferenteId() {
		Respuesta request = new Respuesta();
		request.setId(1234L);
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		Respuesta updated = new Respuesta();
		updated.setId(9L);

		when(respuestaService.updateRespuesta(eq(9L), eq("Nueva"), eq("img2.png"), eq(false))).thenReturn(updated);

		ResponseEntity<Respuesta> response = controller.updateRespuesta(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(updated);
		verify(respuestaService).updateRespuesta(9L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método updateRespuesta lanza NullPointerException y no llama al servicio 
    // cuando se le pasa una request nula
	@Test
	void updateRespuesta_cuandoRequestEsNull_lanzaNullPointer() {
		assertThatThrownBy(() -> controller.updateRespuesta(1L, null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respuestaService);
	}

    // Test para verificar que el método updateRespuesta permitte null en el campo imagen y 
    // delega correctamente al servicio
	@Test
	void updateRespuesta_permiteImagenNull() {
		Respuesta request = new Respuesta();
		request.setRespuesta("Nueva");
		request.setImagen(null);
		request.setCorrecta(true);

		Respuesta updated = new Respuesta();
		updated.setId(9L);

		when(respuestaService.updateRespuesta(eq(9L), eq("Nueva"), eq(null), eq(true))).thenReturn(updated);

		ResponseEntity<Respuesta> response = controller.updateRespuesta(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(updated);
		verify(respuestaService).updateRespuesta(9L, "Nueva", null, true);
	}

    // Test para verificar que el método updateRespuesta propaga AccessDeniedException lanzada por el servicio
	void updateRespuesta_propagaAccessDeniedFromService() {
		Respuesta request = new Respuesta();
		request.setRespuesta("Nueva");
		request.setImagen(null);
		request.setCorrecta(true);

		when(respuestaService.updateRespuesta(eq(1L), eq("Nueva"), eq(null), eq(true)))
				.thenThrow(new AccessDeniedException("Solo maestros"));

		assertThatThrownBy(() -> controller.updateRespuesta(1L, request))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).updateRespuesta(1L, "Nueva", null, true);
	}

    // Test para verificar que el método updateRespuesta propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void updateRespuesta_propagaNotFoundFromService() {
		Respuesta request = new Respuesta();
		request.setRespuesta("Nueva");
		request.setImagen("img2.png");
		request.setCorrecta(false);

		when(respuestaService.updateRespuesta(eq(404L), eq("Nueva"), eq("img2.png"), eq(false)))
				.thenThrow(new ResourceNotFoundException("Respuesta", "id", 404L));

		assertThatThrownBy(() -> controller.updateRespuesta(404L, request))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).updateRespuesta(404L, "Nueva", "img2.png", false);
	}

    // Test para verificar que el método deleteRespuesta devuelve NO_CONTENT y delega correctamente al servicio
	@Test
	void deleteRespuesta_devuelveNoContent() {
		doNothing().when(respuestaService).deleteRespuesta(3L);

		ResponseEntity<Void> response = controller.deleteRespuesta(3L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respuestaService).deleteRespuesta(3L);
	}

    // Test para verificar que el método deleteRespuesta propaga ResourceNotFoundException lanzada por el servicio
	@Test
	void deleteRespuesta_propagaNotFoundFromService() {
		doThrow(new ResourceNotFoundException("Respuesta", "id", 3L))
				.when(respuestaService)
				.deleteRespuesta(3L);

		assertThatThrownBy(() -> controller.deleteRespuesta(3L))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(respuestaService).deleteRespuesta(3L);
	}

    // Test para verificar que el método deleteRespuesta propaga AccessDeniedException lanzada por el servicio
	@Test
	void deleteRespuesta_propagaAccessDeniedFromService() {
		doThrow(new AccessDeniedException("Solo maestros"))
				.when(respuestaService)
				.deleteRespuesta(55L);

		assertThatThrownBy(() -> controller.deleteRespuesta(55L))
				.isInstanceOf(AccessDeniedException.class);
		verify(respuestaService).deleteRespuesta(55L);
	}
}
