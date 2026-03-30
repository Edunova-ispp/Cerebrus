package com.cerebrus.marcarImagenTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.actividad.marcarImagen.*;
import com.cerebrus.actividad.marcarImagen.dto.MarcarImagenDTO;
import com.cerebrus.puntoImagen.dto.PuntoImagenDTO;
import com.cerebrus.tema.Tema;

@ExtendWith(MockitoExtension.class)
class MarcarImagenControllerTest {

	@Mock
	private MarcarImagenService marcarImagenService;

	@InjectMocks
	private MarcarImagenController marcarImagenController;

	@Test
	void crearActMarcarImagen_requestValido_devuelve201ConDTO() {
		MarcarImagenDTO request = crearDTO(1L, 10L, true, List.of());
		MarcarImagen marcarImagen = new MarcarImagen();
		marcarImagen.setId(1L);
		Tema tema = new Tema();
		tema.setId(10L);
		marcarImagen.setTema(tema);
		when(marcarImagenService.crearActMarcarImagen(request)).thenReturn(marcarImagen);
		ResponseEntity<MarcarImagenDTO> response = marcarImagenController.crearActMarcarImagen(request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		verify(marcarImagenService).crearActMarcarImagen(request);
	}

	@Test
	void crearActMarcarImagen_requestNull_lanzaNullPointerException() {
		assertThatThrownBy(() -> marcarImagenController.crearActMarcarImagen(null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void crearActMarcarImagen_serviceLanzaExcepcion_devuelveError() {
		MarcarImagenDTO request = crearDTO(1L, 10L, true, List.of());
		when(marcarImagenService.crearActMarcarImagen(request)).thenThrow(new IllegalArgumentException("Datos inválidos"));
		assertThatThrownBy(() -> marcarImagenController.crearActMarcarImagen(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Datos inválidos");
	}

	@Test
	void encontrarActMarcarImagenPorId_devuelve200ConDTO() {
		MarcarImagen marcarImagen = new MarcarImagen();
		marcarImagen.setId(2L);
		Tema tema = new Tema();
		tema.setId(20L);
		marcarImagen.setTema(tema);
		when(marcarImagenService.encontrarActMarcarImagenPorId(2L)).thenReturn(marcarImagen);
		ResponseEntity<MarcarImagenDTO> response = marcarImagenController.encontrarActMarcarImagenPorId(2L);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		verify(marcarImagenService).encontrarActMarcarImagenPorId(2L);
	}

	@Test
	void encontrarActMarcarImagenPorId_noExiste_lanzaExcepcion() {
		when(marcarImagenService.encontrarActMarcarImagenPorId(99L)).thenThrow(new IllegalArgumentException("No encontrada"));
		assertThatThrownBy(() -> marcarImagenController.encontrarActMarcarImagenPorId(99L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("No encontrada");
	}

	@Test
	void actualizarActMarcarImagen_requestValido_devuelve200ConDTO() {
		MarcarImagenDTO request = crearDTO(3L, 20L, false, List.of());
		MarcarImagen marcarImagen = new MarcarImagen();
		marcarImagen.setId(3L);
		Tema tema = new Tema();
		tema.setId(20L);
		marcarImagen.setTema(tema);
		when(marcarImagenService.actualizarActMarcarImagen(3L, request)).thenReturn(marcarImagen);
		ResponseEntity<MarcarImagenDTO> response = marcarImagenController.actualizarActMarcarImagen(3L, request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		verify(marcarImagenService).actualizarActMarcarImagen(3L, request);
	}

	@Test
	void actualizarActMarcarImagen_requestNull_lanzaNullPointerException() {
		assertThatThrownBy(() -> marcarImagenController.actualizarActMarcarImagen(1L, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void actualizarActMarcarImagen_serviceLanzaExcepcion_lanzaError() {
		MarcarImagenDTO request = crearDTO(4L, 30L, true, List.of());
		when(marcarImagenService.actualizarActMarcarImagen(4L, request)).thenThrow(new IllegalArgumentException("No se puede actualizar"));
		assertThatThrownBy(() -> marcarImagenController.actualizarActMarcarImagen(4L, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("No se puede actualizar");
	}

	@Test
	void eliminarActMarcarImagenPorId_devuelve204NoContent() {
		ResponseEntity<Void> response = marcarImagenController.eliminarActMarcarImagenPorId(4L);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(marcarImagenService).eliminarActMarcarImagenPorId(4L);
	}

	@Test
	void eliminarActMarcarImagenPorId_noExiste_lanzaExcepcion() {
		doThrow(new IllegalArgumentException("No existe")).when(marcarImagenService).eliminarActMarcarImagenPorId(100L);
		assertThatThrownBy(() -> marcarImagenController.eliminarActMarcarImagenPorId(100L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("No existe");
	}

	// Casos límite y condicionalidad
	@Test
	void crearActMarcarImagen_tituloVacio_devuelveError() {
		MarcarImagenDTO requestVacio = new MarcarImagenDTO(1L, "", "desc", 1, "img.png", true, "c", 10L, "img", List.of());
		when(marcarImagenService.crearActMarcarImagen(requestVacio)).thenThrow(new IllegalArgumentException("Título vacío"));
		assertThatThrownBy(() -> marcarImagenController.crearActMarcarImagen(requestVacio))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Título vacío");
	}

	@Test
	void crearActMarcarImagen_puntuacionLimiteInferior() {
		MarcarImagenDTO request = new MarcarImagenDTO(1L, "T", "D", 0, "img.png", true, "c", 10L, "img", List.of());
		MarcarImagen marcarImagen = new MarcarImagen();
		marcarImagen.setId(1L);
		Tema tema = new Tema();
		tema.setId(10L);
		marcarImagen.setTema(tema);
		when(marcarImagenService.crearActMarcarImagen(request)).thenReturn(marcarImagen);
		ResponseEntity<MarcarImagenDTO> response = marcarImagenController.crearActMarcarImagen(request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	void crearActMarcarImagen_puntuacionLimiteSuperior() {
		MarcarImagenDTO request = new MarcarImagenDTO(1L, "T", "D", Integer.MAX_VALUE, "img.png", true, "c", 10L, "img", List.of());
		MarcarImagen marcarImagen = new MarcarImagen();
		marcarImagen.setId(1L);
		Tema tema = new Tema();
		tema.setId(10L);
		marcarImagen.setTema(tema);
		when(marcarImagenService.crearActMarcarImagen(request)).thenReturn(marcarImagen);
		ResponseEntity<MarcarImagenDTO> response = marcarImagenController.crearActMarcarImagen(request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	// Helper para crear MarcarImagenDTO
	private static MarcarImagenDTO crearDTO(Long id, Long temaId, boolean respVisible, List<PuntoImagenDTO> puntos) {
		return new MarcarImagenDTO(
			id,
			"Titulo",
			"Descripcion",
			5,
			"img.png",
			respVisible,
			"comentario",
			temaId,
			"imgAMarcar.png",
			puntos
		);
	}
}
