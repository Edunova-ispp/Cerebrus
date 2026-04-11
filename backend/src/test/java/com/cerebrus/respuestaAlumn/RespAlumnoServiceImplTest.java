package com.cerebrus.respuestaAlumn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cerebrus.actividad.general.General;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.usuario.alumno.Alumno;

@ExtendWith(MockitoExtension.class)
class RespAlumnoServiceImplTest {

	@Mock
	private RespuestaAlumnoRepository respuestaAlumnoRepository;

	@InjectMocks
	private RespuestaAlumnoServiceImpl service;

	@Captor
	private ArgumentCaptor<RespuestaAlumno> respuestaCaptor;

	private RespAlumnoGeneral respuestaAlumno;

	@BeforeEach
	void setUp() {
		respuestaAlumno = crearRespuestaAlumno(null);
	}

	@Test
	void encontrarRespuestaAlumnoPorId_devuelveRespuesta_cuandoExiste() {
		respuestaAlumno.setId(10L);
		when(respuestaAlumnoRepository.findById(10L)).thenReturn(Optional.of(respuestaAlumno));

		RespuestaAlumno encontrada = service.encontrarRespuestaAlumnoPorId(10L);

		assertThat(encontrada).isSameAs(respuestaAlumno);
		verify(respuestaAlumnoRepository).findById(10L);
	}

	@Test
	void encontrarRespuestaAlumnoPorId_lanzaResourceNotFound_cuandoNoExiste() {
		when(respuestaAlumnoRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.encontrarRespuestaAlumnoPorId(404L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("RespuestaAlumno")
				.hasMessageContaining("404");

		verify(respuestaAlumnoRepository).findById(404L);
	}

	@Test
	void marcarODesmarcarRespuestaCorrecta_marcaComoCorrecta_cuandoEraNull() {
		respuestaAlumno.setId(1L);
		respuestaAlumno.setCorrecta(null);
		when(respuestaAlumnoRepository.findById(1L)).thenReturn(Optional.of(respuestaAlumno));
		when(respuestaAlumnoRepository.save(any(RespuestaAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

		RespuestaAlumno actualizada = service.marcarODesmarcarRespuestaCorrecta(1L);

		assertThat(actualizada).isSameAs(respuestaAlumno);
		assertThat(actualizada.getCorrecta()).isTrue();
		verify(respuestaAlumnoRepository).findById(1L);
		verify(respuestaAlumnoRepository).save(respuestaCaptor.capture());
		assertThat(respuestaCaptor.getValue().getCorrecta()).isTrue();
	}

	@Test
	void marcarODesmarcarRespuestaCorrecta_desmarca_cuandoEraTrue() {
		respuestaAlumno.setId(2L);
		respuestaAlumno.setCorrecta(true);
		when(respuestaAlumnoRepository.findById(2L)).thenReturn(Optional.of(respuestaAlumno));
		when(respuestaAlumnoRepository.save(any(RespuestaAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

		RespuestaAlumno actualizada = service.marcarODesmarcarRespuestaCorrecta(2L);

		assertThat(actualizada.getCorrecta()).isFalse();
		verify(respuestaAlumnoRepository).save(respuestaCaptor.capture());
		assertThat(respuestaCaptor.getValue().getCorrecta()).isFalse();
	}

	@Test
	void marcarODesmarcarRespuestaCorrecta_marca_cuandoEraFalse() {
		respuestaAlumno.setId(3L);
		respuestaAlumno.setCorrecta(false);
		when(respuestaAlumnoRepository.findById(3L)).thenReturn(Optional.of(respuestaAlumno));
		when(respuestaAlumnoRepository.save(any(RespuestaAlumno.class))).thenAnswer(inv -> inv.getArgument(0));

		RespuestaAlumno actualizada = service.marcarODesmarcarRespuestaCorrecta(3L);

		assertThat(actualizada.getCorrecta()).isTrue();
		verify(respuestaAlumnoRepository).save(respuestaCaptor.capture());
		assertThat(respuestaCaptor.getValue().getCorrecta()).isTrue();
	}

	@Test
	void marcarODesmarcarRespuestaCorrecta_lanzaResourceNotFound_cuandoNoExiste() {
		when(respuestaAlumnoRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.marcarODesmarcarRespuestaCorrecta(999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("RespuestaAlumno")
				.hasMessageContaining("999");

		verify(respuestaAlumnoRepository).findById(999L);
		verify(respuestaAlumnoRepository, never()).save(any());
	}

	private static RespAlumnoGeneral crearRespuestaAlumno(Boolean correcta) {
		Alumno alumno = new Alumno();
		alumno.setId(1L);

		General actividad = new General();
		actividad.setId(10L);

		ActividadAlumno actividadAlumno = new ActividadAlumno();
		actividadAlumno.setId(20L);
		actividadAlumno.setAlumno(alumno);
		actividadAlumno.setActividad(actividad);

		return new RespAlumnoGeneral(correcta, actividadAlumno, "respuesta", null);
	}
}
