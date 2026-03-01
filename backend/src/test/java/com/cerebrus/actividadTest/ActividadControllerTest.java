package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadController;
import com.cerebrus.actividad.ActividadService;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

	@Mock
	private ActividadService actividadService;

	@InjectMocks
	private ActividadController actividadController;

    // Tests para verificar que se devuelve 201 con la actividad creada si el request es válido
	@Test
	void crearActividadTeoria_requestValido_devuelve201ConActividad() {
		ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
		request.setTitulo("Titulo");
		request.setDescripcion("Desc");
		request.setPuntuacion(10);
		request.setImagen("img.png");
		request.setTemaId(5L);

		Actividad actividad = new Actividad() {};
		actividad.setId(99L);

		when(actividadService.crearActividadTeoria("Titulo", "Desc", 10, "img.png", 5L, 7L))
				.thenReturn(actividad);

		ResponseEntity<Actividad> response = actividadController.crearActividadTeoria(request, 7L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(actividad);
		verify(actividadService).crearActividadTeoria("Titulo", "Desc", 10, "img.png", 5L, 7L);
	}

    // Tests para verificar que se devuelve 400 si el servicio lanza IllegalArgumentException
	@Test
	void crearActividadTeoria_imagenNull_devuelve201() {
		ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
		request.setTitulo("Titulo");
		request.setDescripcion("Desc");
		request.setPuntuacion(10);
		request.setImagen(null);
		request.setTemaId(5L);

		Actividad actividad = new Actividad() {};
		when(actividadService.crearActividadTeoria(eq("Titulo"), eq("Desc"), eq(10), eq(null), eq(5L), eq(7L)))
				.thenReturn(actividad);

		ResponseEntity<Actividad> response = actividadController.crearActividadTeoria(request, 7L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isSameAs(actividad);
	}

    // Tests para verificar que se devuelve 400 si el servicio lanza IllegalArgumentException
	@Test
	void crearActividadTeoria_serviceLanzaIllegalArgumentException_devuelve400() {
		ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(1);
		request.setImagen("img");
		request.setTemaId(10L);

		when(actividadService.crearActividadTeoria(any(), any(), any(), any(), any(), any()))
				.thenThrow(new IllegalArgumentException("Tema no encontrado"));

		ResponseEntity<Actividad> response = actividadController.crearActividadTeoria(request, 1L);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

    // Tests para verificar que se lanza NullPointerException si el request es null, y no se llama al servicio
	@Test
	void crearActividadTeoria_requestNull_lanzaNullPointerException_yNoLlamaService() {
		assertThatThrownBy(() -> actividadController.crearActividadTeoria(null, 1L))
				.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(actividadService);
	}

    // Tests para verificar que se lanza IllegalArgumentException si maestroId es null, y no se llama al servicio
	@Test
	void crearActividadTeoria_maestroIdNull_serviceLanzaIllegalArgumentException() {
		ActividadController.CrearActividadTeoriaRequest request = new ActividadController.CrearActividadTeoriaRequest();
		request.setTitulo("T");
		request.setDescripcion("D");
		request.setPuntuacion(1);
		request.setImagen("img");
		request.setTemaId(10L);

		when(actividadService.crearActividadTeoria("T", "D", 1, "img", 10L, null))
				.thenThrow(new IllegalArgumentException("maestroId requerido"));

		ResponseEntity<Actividad> response = actividadController.crearActividadTeoria(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
		verify(actividadService).crearActividadTeoria("T", "D", 1, "img", 10L, null);
	}
}
