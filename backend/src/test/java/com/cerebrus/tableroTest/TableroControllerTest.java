package com.cerebrus.tableroTest;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.cerebrus.actividad.tablero.TableroController;
import com.cerebrus.actividad.tablero.TableroService;
import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.actividad.tablero.dto.TableroRequest;
import com.fasterxml.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
public class TableroControllerTest {

	private MockMvc mockMvc;

	@Mock
	private TableroService tableroService;

	@InjectMocks
	private TableroController tableroController;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(tableroController).build();
	}

	@Test
	void crearActividadTablero_ok_8_preguntas() throws Exception {
		TableroRequest req = buildTableroRequest(8, true);
		TableroDTO dto = new TableroDTO(
			1L, "titulo", "descripcion", true, 1, 100, true, 1L, new java.util.ArrayList<>()
		);
		when(tableroService.crearActividadTablero(any())).thenReturn(dto);
		mockMvc.perform(post("/api/tableros")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated());
	}

	@Test
	void crearActividadTablero_ok_15_preguntas() throws Exception {
		TableroRequest req = buildTableroRequest(15, false);
		TableroDTO dto = new TableroDTO(
			2L, "titulo", "descripcion", false, 2, 200, false, 2L, new java.util.ArrayList<>()
		);
		when(tableroService.crearActividadTablero(any())).thenReturn(dto);
		mockMvc.perform(post("/api/tableros")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated());
	}

	@Test
	void crearActividadTablero_badRequest_preguntasInvalidas() throws Exception {
		TableroRequest req = buildTableroRequest(7, true);
		mockMvc.perform(post("/api/tableros")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getTablero_ok() throws Exception {
		TableroDTO dto = new TableroDTO(
			3L, // id
			"titulo",
			"descripcion",
			true,
			3,
			300,
			true,
			3L,
			new java.util.ArrayList<>()
		);
		when(tableroService.getTablero(1L)).thenReturn(dto);
		mockMvc.perform(get("/api/tableros/1"))
				.andExpect(status().isOk());
	}

	@Test
	void eliminarTablero_ok() throws Exception {
		doNothing().when(tableroService).eliminarTablero(1L);
		mockMvc.perform(delete("/api/tableros/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void actualizarTablero_ok_8_preguntas() throws Exception {
		TableroRequest req = buildTableroRequest(8, true);
		TableroDTO dto = new TableroDTO(
			4L, "titulo", "descripcion", true, 4, 400, true, 4L, new java.util.ArrayList<>()
		);
		when(tableroService.actualizarTablero(eq(1L), any())).thenReturn(dto);
		mockMvc.perform(put("/api/tableros/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk());
	}

	@Test
	void actualizarTablero_badRequest_preguntasInvalidas() throws Exception {
		TableroRequest req = buildTableroRequest(7, true);
		mockMvc.perform(put("/api/tableros/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void crearRespuestaAPreguntaTablero_ok() throws Exception {
		when(tableroService.crearRespuestaAPreguntaTablero(anyString(), eq(1L), eq(2L))).thenReturn("Respuesta correcta");
		mockMvc.perform(post("/api/tableros/1/2")
				.contentType(MediaType.APPLICATION_JSON)
				.content("\"respuesta\""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Respuesta correcta")));
	}

		@Test
		void crearActividadTablero_badRequest_numPreguntasNo8Ni15() throws Exception {
			TableroRequest req = buildTableroRequest(10, true);
			mockMvc.perform(post("/api/tableros")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest());
		}

	@Test
	void crearActividadTablero_badRequest_tamanoTruePeroNo8() throws Exception {
		TableroRequest req = buildTableroRequest(15, true);
		mockMvc.perform(post("/api/tableros")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void crearActividadTablero_badRequest_tamanoFalsePeroNo15() throws Exception {
		TableroRequest req = buildTableroRequest(8, false);
		mockMvc.perform(post("/api/tableros")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

		@Test
		void actualizarTablero_badRequest_numPreguntasNo8Ni15() throws Exception {
			TableroRequest req = buildTableroRequest(10, true);
			mockMvc.perform(put("/api/tableros/1")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest());
		}

	@Test
	void actualizarTablero_badRequest_tamanoTruePeroNo8() throws Exception {
		TableroRequest req = buildTableroRequest(15, true);
		mockMvc.perform(put("/api/tableros/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizarTablero_badRequest_tamanoFalsePeroNo15() throws Exception {
		TableroRequest req = buildTableroRequest(8, false);
		mockMvc.perform(put("/api/tableros/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

    private TableroRequest buildTableroRequest(int numPreguntas, boolean tamano) throws Exception {
		TableroRequest req = new TableroRequest();
		Field f = TableroRequest.class.getDeclaredField("preguntasYRespuestas");
		f.setAccessible(true);
		LinkedHashMap<String, String> preguntas = new LinkedHashMap<>();
		for (int i = 0; i < numPreguntas; i++) preguntas.put("P"+i, "R"+i);
		f.set(req, preguntas);

		Field t = TableroRequest.class.getDeclaredField("tamano");
		t.setAccessible(true);
		t.set(req, tamano);

		Field titulo = TableroRequest.class.getDeclaredField("titulo");
		titulo.setAccessible(true);
		titulo.set(req, "Titulo Test");

		Field puntuacion = TableroRequest.class.getDeclaredField("puntuacion");
		puntuacion.setAccessible(true);
		puntuacion.set(req, 10);

		Field temaId = TableroRequest.class.getDeclaredField("temaId");
		temaId.setAccessible(true);
		temaId.set(req, 1L);

		Field respVisible = TableroRequest.class.getDeclaredField("respVisible");
		respVisible.setAccessible(true);
		respVisible.set(req, true);

		Field descripcion = TableroRequest.class.getDeclaredField("descripcion");
		descripcion.setAccessible(true);
		descripcion.set(req, "Descripcion Test");

		return req;
	}

}
