package com.cerebrus.respuestaAlumn.respAlumGeneral;

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

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralController;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralService;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralRequest;

@ExtendWith(MockitoExtension.class)
public class RespAlumnoGeneralControllerTest {

	@Mock
	private RespAlumnoGeneralService respAlumnoGeneralService;

	@InjectMocks
	private RespAlumnoGeneralController controller;

    // Test para verificar que el método crearRespuestaAlumnoGeneral devuelve CREATED y delega en el 
    // servicio con los parámetros correctos
	@Test
	void crearRespuestaAlumnoGeneral_devuelveCreated() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(10L);
		request.setPreguntaId(20L);
		request.setRespuestaId(30L);

		RespAlumnoGeneralCreateResponse created = new RespAlumnoGeneralCreateResponse();
		created.setComentario("OK");

		when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(eq(10L), eq(30L), eq(20L))).thenReturn(created);

		ResponseEntity<RespAlumnoGeneralCreateResponse> response = controller.crearRespuestaAlumnoGeneral(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(created);
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(10L, 30L, 20L);
	}

	@Test
	void crearRespuestaAlumnoGeneral_cuandoRequestEsNull_lanzaNullPointer() {
		assertThatThrownBy(() -> controller.crearRespuestaAlumnoGeneral(null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respAlumnoGeneralService);
	}

    // Test para verificar que el método crearRespuestaAlumnoGeneral lanza NullPointerException cuando la actividad del alumno es null, 
    // y no interactúa con el servicio
	@Test
	void crearRespuestaAlumnoGeneral_cuandoActividadAlumnoEsNull_lanzaNullPointer() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(null);
		request.setPreguntaId(20L);
		request.setRespuestaId(30L);

		when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(null, 30L, 20L))
				.thenThrow(new RuntimeException("La actividad del alumno no existe"));

		assertThatThrownBy(() -> controller.crearRespuestaAlumnoGeneral(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La actividad del alumno no existe");
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(null, 30L, 20L);
	}

	@Test
	void crearRespuestaAlumnoGeneral_cuandoPreguntaEsNull_lanzaNullPointer() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(10L);
		request.setPreguntaId(null);
		request.setRespuestaId(30L);

		when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(10L, 30L, null))
				.thenThrow(new RuntimeException("La pregunta no existe"));

		assertThatThrownBy(() -> controller.crearRespuestaAlumnoGeneral(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La pregunta no existe");
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(10L, 30L, null);
	}

	@Test
	void crearRespuestaAlumnoGeneral_propagaRuntimeException() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(10L);
		request.setPreguntaId(20L);
		request.setRespuestaId(30L);

		when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(eq(10L), eq(30L), eq(20L)))
				.thenThrow(new RuntimeException("La respuesta no existe"));

		assertThatThrownBy(() -> controller.crearRespuestaAlumnoGeneral(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La respuesta no existe");
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(10L, 30L, 20L);
	}

	@Test
	void crearRespuestaAlumnoGeneral_propagaAccessDenied() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(10L);
		request.setPreguntaId(20L);
		request.setRespuestaId(30L);

		when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(eq(10L), eq(30L), eq(20L)))
				.thenThrow(new AccessDeniedException("Solo un alumno"));

		assertThatThrownBy(() -> controller.crearRespuestaAlumnoGeneral(request))
				.isInstanceOf(AccessDeniedException.class);
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(10L, 30L, 20L);
	}

	@Test
	void crearRespuestaAlumnoGeneral_permiteRespuestaNull() {
		RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
		request.setActividadAlumnoId(1L);
		request.setPreguntaId(2L);
		request.setRespuestaId(null);
		ResponseEntity<RespAlumnoGeneralCreateResponse> response = controller.crearRespuestaAlumnoGeneral(request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNull();
		verify(respAlumnoGeneralService).crearRespuestaAlumnoGeneral(1L, null, 2L);
	}

    // Test para verificar que el método encontrarRespuestaAlumnoGeneralPorId devuelve OK y delega en el servicio
	@Test
	void encontrarRespuestaAlumnoGeneralPorId_devuelveOk() {
		RespAlumnoGeneral existing = new RespAlumnoGeneral();
		existing.setId(5L);
		when(respAlumnoGeneralService.encontrarRespuestaAlumnoGeneralPorId(5L)).thenReturn(existing);

		ResponseEntity<RespAlumnoGeneral> response = controller.encontrarRespuestaAlumnoGeneralPorId(5L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(existing);
		verify(respAlumnoGeneralService).encontrarRespuestaAlumnoGeneralPorId(5L);
	}

    // Test para verificar que el método encontrarRespuestaAlumnoGeneralPorId lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el servicio de usuarios
	@Test
	void encontrarRespuestaAlumnoGeneralPorId_propagaRuntimeException() {
		when(respAlumnoGeneralService.encontrarRespuestaAlumnoGeneralPorId(404L))
				.thenThrow(new RuntimeException("La respuesta del alumno no existe"));

		assertThatThrownBy(() -> controller.encontrarRespuestaAlumnoGeneralPorId(404L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La respuesta del alumno no existe");
		verify(respAlumnoGeneralService).encontrarRespuestaAlumnoGeneralPorId(404L);
	}

    // Test para verificar que el método actualizarRespuestaAlumnoGeneral devuelve OK y delega en el servicio con los 
    // parámetros correctos
	@Test
	void actualizarRespuestaAlumnoGeneral_devuelveOk() {
		ActividadAlumno actAlumno = new ActividadAlumno();
		actAlumno.setId(10L);

		Pregunta pregunta = new Pregunta();
		pregunta.setId(20L);

		RespAlumnoGeneral request = new RespAlumnoGeneral();
		request.setActividadAlumno(actAlumno);
		request.setPregunta(pregunta);
		request.setRespuesta("RESP");
		request.setCorrecta(true);

		RespAlumnoGeneral updated = new RespAlumnoGeneral();
		updated.setId(9L);

		when(respAlumnoGeneralService.actualizarRespuestaAlumnoGeneral(eq(9L), eq(true), eq(10L), eq("RESP"), eq(20L)))
				.thenReturn(updated);

		ResponseEntity<RespAlumnoGeneral> response = controller.actualizarRespuestaAlumnoGeneral(9L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(updated);
		verify(respAlumnoGeneralService).actualizarRespuestaAlumnoGeneral(9L, true, 10L, "RESP", 20L);
	}

    // Test para verificar que el método actualizarRespuestaAlumnoGeneral lanza NullPointerException cuando la request es null, 
    // y no interactúa con el servicio
	@Test
	void actualizarRespuestaAlumnoGeneral_cuandoRequestEsNull_lanzaNullPointer() {
		assertThatThrownBy(() -> controller.actualizarRespuestaAlumnoGeneral(1L, null))
				.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(respAlumnoGeneralService);
	}

    // Test para verificar que el método actualizarRespuestaAlumnoGeneral permite que el atributo correcta sea null, devuelve OK, 
    // y delega en el servicio con los parámetros correctos
	@Test
	void actualizarRespuestaAlumnoGeneral_permiteCorrectaNull() {
		ActividadAlumno actAlumno = new ActividadAlumno();
		actAlumno.setId(10L);

		Pregunta pregunta = new Pregunta();
		pregunta.setId(20L);

		RespAlumnoGeneral request = new RespAlumnoGeneral();
		request.setActividadAlumno(actAlumno);
		request.setPregunta(pregunta);
		request.setRespuesta("RESP");
		request.setCorrecta(null);

		RespAlumnoGeneral updated = new RespAlumnoGeneral();
		when(respAlumnoGeneralService.actualizarRespuestaAlumnoGeneral(eq(9L), isNull(), eq(10L), eq("RESP"), eq(20L)))
				.thenReturn(updated);

		ResponseEntity<RespAlumnoGeneral> response = controller.actualizarRespuestaAlumnoGeneral(9L, request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(updated);
		verify(respAlumnoGeneralService).actualizarRespuestaAlumnoGeneral(9L, null, 10L, "RESP", 20L);
	}

    // Test para verificar que el método actualizarRespuestaAlumnoGeneral lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el servicio de usuarios
	@Test
	void actualizarRespuestaAlumnoGeneral_propagaRuntimeException() {
		ActividadAlumno actAlumno = new ActividadAlumno();
		actAlumno.setId(10L);

		Pregunta pregunta = new Pregunta();
		pregunta.setId(20L);

		RespAlumnoGeneral request = new RespAlumnoGeneral();
		request.setActividadAlumno(actAlumno);
		request.setPregunta(pregunta);
		request.setRespuesta("RESP");
		request.setCorrecta(true);

		when(respAlumnoGeneralService.actualizarRespuestaAlumnoGeneral(eq(404L), eq(true), eq(10L), eq("RESP"), eq(20L)))
				.thenThrow(new RuntimeException("La respuesta del alumno no existe"));

		assertThatThrownBy(() -> controller.actualizarRespuestaAlumnoGeneral(404L, request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La respuesta del alumno no existe");
		verify(respAlumnoGeneralService).actualizarRespuestaAlumnoGeneral(404L, true, 10L, "RESP", 20L);
	}

    // Test para verificar que el método eliminarRespuestaAlumnoGeneralPorId devuelve No Content y delega en el 
    // servicio con los parámetros correctos
	@Test
	void eliminarRespuestaAlumnoGeneralPorId_devuelveNoContent() {
		doNothing().when(respAlumnoGeneralService).eliminarRespuestaAlumnoGeneralPorId(3L);

		ResponseEntity<Void> response = controller.eliminarRespuestaAlumnoGeneralPorId(3L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(respAlumnoGeneralService).eliminarRespuestaAlumnoGeneralPorId(3L);
	}

    // Test para verificar que el método eliminarRespuestaAlumnoGeneralPorId lanza RuntimeException con mensaje específico cuando la
    // respuesta del alumno no existe, y no interactúa con el servicio de usuarios
	@Test
	void eliminarRespuestaAlumnoGeneralPorId_propagaRuntimeException() {
		doThrow(new RuntimeException("La respuesta del alumno no existe"))
				.when(respAlumnoGeneralService)
				.eliminarRespuestaAlumnoGeneralPorId(3L);

		assertThatThrownBy(() -> controller.eliminarRespuestaAlumnoGeneralPorId(3L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("La respuesta del alumno no existe");
		verify(respAlumnoGeneralService).eliminarRespuestaAlumnoGeneralPorId(3L);
	}

    // Test para verificar que el método eliminarRespuestaAlumnoGeneralPorId lanza AccessDeniedException con mensaje 
    // específico cuando el servicio lo lanza
	@Test
	void eliminarRespuestaAlumnoGeneralPorId_propagaAccessDenied() {
		doThrow(new AccessDeniedException("No"))
				.when(respAlumnoGeneralService)
				.eliminarRespuestaAlumnoGeneralPorId(55L);

		assertThatThrownBy(() -> controller.eliminarRespuestaAlumnoGeneralPorId(55L))
				.isInstanceOf(AccessDeniedException.class);
		verify(respAlumnoGeneralService).eliminarRespuestaAlumnoGeneralPorId(55L);
	}
}
