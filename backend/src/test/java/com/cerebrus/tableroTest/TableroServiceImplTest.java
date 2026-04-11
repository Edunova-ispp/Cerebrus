package com.cerebrus.tableroTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.tablero.Tablero;
import com.cerebrus.actividad.tablero.TableroRepository;
import com.cerebrus.actividad.tablero.TableroServiceImpl;
import com.cerebrus.actividad.tablero.dto.TableroDTO;
import com.cerebrus.actividad.tablero.dto.TableroRequest;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.actividadAlumn.ActividadAlumnoService;
import com.cerebrus.comun.enumerados.TamanoTablero;
import com.cerebrus.curso.Curso;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralRepository;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
public class TableroServiceImplTest {

	@Mock 
    private TableroRepository tableroRepository;

	@Mock 
    private ActividadRepository actividadRepository;

	@Mock 
    private TemaRepository temaRepository;

	@Mock 
    private PreguntaRepository preguntaRepository;

	@Mock 
    private RespuestaMaestroRepository respuestaMaestroRepository;

	@Mock 
    private UsuarioService usuarioService;

	@Mock 
    private ActividadAlumnoService actividadAlumnoService;

	@Mock 
    private RespAlumnoGeneralRepository respuestaAlumnoRepository;

	@Mock 
    private ActividadAlumnoRepository actividadAlumnoRepository;

	@InjectMocks
	private TableroServiceImpl tableroService;

	private Maestro maestro;
	private Tema tema;
	private TableroRequest request;

	@BeforeEach
	void setUp() {
		maestro = mock(Maestro.class);
		tema = mock(Tema.class);
		request = mock(TableroRequest.class);
	}

	@Test
	void crearActividadTablero_ok_maestroValido() {
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(request.getTemaId()).thenReturn(1L);
		when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
		when(tema.getCurso()).thenReturn(mock(Curso.class));
		when(tema.getCurso().getMaestro()).thenReturn(maestro);
		when(maestro.getId()).thenReturn(2L);
		when(tema.getCurso().getMaestro().getId()).thenReturn(2L);
		when(actividadRepository.findMaxPosicionByTemaId(1L)).thenReturn(0);
		when(request.getTitulo()).thenReturn("T");
		when(request.getDescripcion()).thenReturn("D");
		when(request.getPuntuacion()).thenReturn(10);
		when(request.getRespVisible()).thenReturn(true);
		when(request.getTamano()).thenReturn(true);
		LinkedHashMap<String, String> preguntas = new LinkedHashMap<>();
		preguntas.put("P1", "R1");
		when(request.getPreguntasYRespuestas()).thenReturn(preguntas);
		when(tableroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		TableroDTO dto = tableroService.crearActTablero(request);
		assertThat(dto).isNotNull();
		assertThat(dto.getTitulo()).isEqualTo("T");
	}

	@Test
	void crearActividadTablero_falla_noMaestro() {
		Usuario user = mock(Usuario.class);
		when(usuarioService.findCurrentUser()).thenReturn(user);
		assertThatThrownBy(() -> tableroService.crearActTablero(request))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void crearActividadTablero_falla_temaNoExiste() {
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(request.getTemaId()).thenReturn(1L);
		when(temaRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.crearActTablero(request))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void crearActividadTablero_falla_maestroNoPropietario() {
		// 1. Configuración de Usuarios (Maestro actual con ID 2)
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(maestro.getId()).thenReturn(2L);
		
		// 2. Configuración del Maestro Propietario (ID 3, diferente para forzar el fallo)
		Maestro otroMaestro = mock(Maestro.class);
		when(otroMaestro.getId()).thenReturn(3L);

		// 3. Configuración del Curso vinculado al "Otro Maestro"
		Curso cursoMock = mock(Curso.class);
		when(cursoMock.getMaestro()).thenReturn(otroMaestro);

		// 4. Configuración del Tema vinculado al Curso
		// Usamos lenient() porque si la excepción salta muy pronto, 
		// Mockito podría quejarse de que no se llegó a usar este stub.
		lenient().when(tema.getCurso()).thenReturn(cursoMock);
		lenient().when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

		// 5. Configuración del Tablero (Para evitar ResourceNotFoundException)
		// El tablero debe estar vinculado al tema para que el Service pueda navegar:
		// tablero -> tema -> curso -> maestro
		Tablero tableroFake = new Tablero();
		tableroFake.setTema(tema); 
		when(tableroRepository.findById(10L)).thenReturn(Optional.of(tableroFake));

		// 6. Ejecución y Verificación
		// Al ser IDs 2 y 3, debe lanzar AccessDeniedException
		assertThatThrownBy(() -> tableroService.encontrarActTableroPorId(10L))
			.isInstanceOf(AccessDeniedException.class);
		
		// Verificamos que NUNCA se llegó a guardar nada por el fallo de seguridad
		verify(tableroRepository, never()).save(any());
	}

	@Test
	void getTablero_ok_maestroPropietario() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(maestro);
		when(maestro.getId()).thenReturn(2L);
		when(curso.getMaestro().getId()).thenReturn(2L);
		when(tablero.getPreguntas()).thenReturn(new ArrayList<>());
		TableroDTO dto = tableroService.encontrarActTableroPorId(1L);
		assertThat(dto).isNotNull();
	}

	@Test
	void getTablero_falla_noExiste() {
		when(tableroRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.encontrarActTableroPorId(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void eliminarTablero_ok_maestroPropietario() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(maestro);
		when(maestro.getId()).thenReturn(2L);
		when(curso.getMaestro().getId()).thenReturn(2L);
		doNothing().when(tableroRepository).delete(tablero);
		tableroService.eliminarActTableroPorId(1L);
		verify(tableroRepository).delete(tablero);
	}

	@Test
	void eliminarTablero_falla_noExiste() {
		when(tableroRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.eliminarActTableroPorId(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void eliminarTablero_falla_noMaestro() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		Usuario user = mock(Usuario.class);
		when(usuarioService.findCurrentUser()).thenReturn(user);
		assertThatThrownBy(() -> tableroService.eliminarActTableroPorId(1L))
			.isInstanceOf(AccessDeniedException.class);
	}

		@Test
		void actualizarTablero_ok_maestroPropietario() {
			Tablero tablero = mock(Tablero.class);
			when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
			when(usuarioService.findCurrentUser()).thenReturn(maestro);
			Tema tema = mock(Tema.class);
			Curso curso = mock(Curso.class);
			when(tablero.getTema()).thenReturn(tema);
			when(tema.getCurso()).thenReturn(curso);
			when(curso.getMaestro()).thenReturn(maestro);
			when(maestro.getId()).thenReturn(2L);
			when(curso.getMaestro().getId()).thenReturn(2L);
			when(tablero.getPreguntas()).thenReturn(new java.util.ArrayList<>());
			when(tablero.getVersion()).thenReturn(1);
			when(tablero.getTitulo()).thenReturn("T");
			TableroRequest req = mock(TableroRequest.class);
			when(req.getTitulo()).thenReturn("T");
			when(req.getDescripcion()).thenReturn("D");
			when(req.getPuntuacion()).thenReturn(10);
			when(req.getRespVisible()).thenReturn(true);
			when(req.getTamano()).thenReturn(true);
			LinkedHashMap<String, String> preguntas = new LinkedHashMap<>();
			preguntas.put("P1", "R1");
			when(req.getPreguntasYRespuestas()).thenReturn(preguntas);
			when(tableroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			TableroDTO dto = tableroService.actualizarActTablero(1L, req);
			assertThat(dto).isNotNull();
			assertThat(dto.getTitulo()).isEqualTo("T");
		}

	@Test
	void actualizarTablero_falla_noExiste() {
		TableroRequest req = mock(TableroRequest.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.actualizarActTablero(1L, req))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void actualizarTablero_falla_noMaestro() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		Usuario user = mock(Usuario.class);
		when(usuarioService.findCurrentUser()).thenReturn(user);
		TableroRequest req = mock(TableroRequest.class);
		assertThatThrownBy(() -> tableroService.actualizarActTablero(1L, req))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void actualizarTablero_falla_maestroNoPropietario() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		Maestro otroMaestro = mock(Maestro.class);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(otroMaestro);
		when(maestro.getId()).thenReturn(2L);
		when(otroMaestro.getId()).thenReturn(3L);
		TableroRequest req = mock(TableroRequest.class);
		assertThatThrownBy(() -> tableroService.actualizarActTablero(1L, req))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void crearRespuestaAPreguntaTablero_ok_respuestaCorrecta() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = buildPreguntaWithRespuestaMaestro(2L, "respuesta");
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, true, true, 10, true);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(pregunta.getActividad()).thenReturn(tablero);
		ActividadAlumno actividadAlumno = mock(ActividadAlumno.class);
		List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();
		when(actividadAlumno.getRespuestasAlumno()).thenReturn(respuestasAlumno);
		when(actividadAlumnoService.crearActAlumno(anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong())).thenReturn(actividadAlumno);
		String result = tableroService.crearRespuestaAPreguntaEnActTablero("respuesta", 1L, 2L);
		assertThat(result).contains("Respuesta correcta");
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_noTablero() {
		when(tableroRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_noPregunta() {
		Tablero tablero = mock(Tablero.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_noAlumno() {
		Tablero tablero = mock(Tablero.class);
		Pregunta pregunta = mock(Pregunta.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		Usuario user = mock(Usuario.class);
		when(usuarioService.findCurrentUser()).thenReturn(user);
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(AccessDeniedException.class);
	}

    @Test
	void getTablero_falla_maestroNoPropietario() {
		Tablero tablero = mock(Tablero.class);
		Maestro maestro = mock(Maestro.class);
		Maestro otroMaestro = mock(Maestro.class);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(otroMaestro);
		when(maestro.getId()).thenReturn(2L);
		when(otroMaestro.getId()).thenReturn(3L);
		assertThatThrownBy(() -> tableroService.encontrarActTableroPorId(1L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("No tienes permiso para acceder a este tablero");
	}

	@Test
	void getTablero_falla_cursoOculto() {
		Tablero tablero = mock(Tablero.class);
		Alumno alumno = mock(Alumno.class);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getVisibilidad()).thenReturn(false);
		assertThatThrownBy(() -> tableroService.encontrarActTableroPorId(1L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("curso oculto");
	}

	@Test
	void getTablero_falla_alumnoNoInscrito() {
		Tablero tablero = mock(Tablero.class);
		Alumno alumno = mock(Alumno.class);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getVisibilidad()).thenReturn(true);
		when(curso.getInscripciones()).thenReturn(Collections.emptyList());
		assertThatThrownBy(() -> tableroService.encontrarActTableroPorId(1L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("no estás inscrito");
	}

	@Test
	void eliminarTablero_falla_maestroNoPropietario() {
		Tablero tablero = mock(Tablero.class);
		Maestro maestro = mock(Maestro.class);
		Maestro otroMaestro = mock(Maestro.class);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(otroMaestro);
		when(maestro.getId()).thenReturn(2L);
		when(otroMaestro.getId()).thenReturn(3L);
		assertThatThrownBy(() -> tableroService.eliminarActTableroPorId(1L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("no eres el maestro del curso");
	}

	@Test
	void actualizarTablero_borraPreguntasExistentes() {
		Tablero tablero = mock(Tablero.class);
		Pregunta pregunta = mock(Pregunta.class);
		List<Pregunta> preguntas = new ArrayList<>();
		preguntas.add(pregunta);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		when(tablero.getTema()).thenReturn(tema);
		when(tema.getCurso()).thenReturn(curso);
		when(curso.getMaestro()).thenReturn(maestro);
		when(maestro.getId()).thenReturn(2L);
		when(curso.getMaestro().getId()).thenReturn(2L);
		when(tablero.getPreguntas()).thenReturn(preguntas).thenReturn(new ArrayList<>());
		when(tablero.getVersion()).thenReturn(1);
		TableroRequest req = mock(TableroRequest.class);
		when(req.getTitulo()).thenReturn("T");
		when(req.getDescripcion()).thenReturn("D");
		when(req.getPuntuacion()).thenReturn(10);
		when(req.getRespVisible()).thenReturn(true);
		when(req.getTamano()).thenReturn(true);
		LinkedHashMap<String, String> preguntasMap = new LinkedHashMap<>();
		preguntasMap.put("P1", "R1");
		when(req.getPreguntasYRespuestas()).thenReturn(preguntasMap);
		when(tableroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(pregunta.getRespuestasMaestro()).thenReturn(new ArrayList<>());
		TableroDTO dto = tableroService.actualizarActTablero(1L, req);
		verify(respuestaMaestroRepository).deleteAll(any());
		verify(preguntaRepository).delete(pregunta);
		assertThat(dto).isNotNull();
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_cursoOculto() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = mock(Pregunta.class);
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, false, true, true, 10, true);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("curso oculto");
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_alumnoNoInscrito() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = mock(Pregunta.class);
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, false, true, 10, true);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("no esta inscrito");
	}

	@Test
	void crearRespuestaAPreguntaTablero_falla_preguntaNoPertenece() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = mock(Pregunta.class);
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, true, true, 10, false);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		assertThatThrownBy(() -> tableroService.crearRespuestaAPreguntaEnActTablero("r", 1L, 2L))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("no pertenece a este tablero");
	}

	@Test
	void crearRespuestaAPreguntaTablero_respuestaConComillas() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = buildPreguntaWithRespuestaMaestro(2L, "respuesta");
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, true, true, 10, true);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(pregunta.getActividad()).thenReturn(tablero);
		ActividadAlumno actividadAlumno = mock(ActividadAlumno.class);
		List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();
		when(actividadAlumno.getRespuestasAlumno()).thenReturn(respuestasAlumno);
		when(actividadAlumnoService.crearActAlumno(anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong())).thenReturn(actividadAlumno);
		String result = tableroService.crearRespuestaAPreguntaEnActTablero("\"respuesta\"", 1L, 2L);
		assertThat(result).contains("Respuesta correcta");
	}

	@Test
	void crearRespuestaAPreguntaTablero_notaPuntuacionMinima() {
		Alumno alumno = mock(Alumno.class);
		// 4 preguntas: p1, p2, p3, pregunta
		Pregunta p1 = buildPreguntaWithRespuestaMaestro(3L, "resp1");
		Pregunta p2 = buildPreguntaWithRespuestaMaestro(4L, "resp2");
		Pregunta p3 = buildPreguntaWithRespuestaMaestro(5L, "resp3");
		Pregunta pregunta = buildPreguntaWithRespuestaMaestro(2L, "respuesta");
		List<Pregunta> preguntas = new ArrayList<>(List.of(p1, p2, p3, pregunta));
		
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, true, true, 1, true);
		when(tablero.getPreguntas()).thenReturn(preguntas);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(preguntaRepository.findById(3L)).thenReturn(Optional.of(p1));
		when(preguntaRepository.findById(4L)).thenReturn(Optional.of(p2));
		when(preguntaRepository.findById(5L)).thenReturn(Optional.of(p3));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(pregunta.getActividad()).thenReturn(tablero);
		when(p1.getActividad()).thenReturn(tablero);
		when(p2.getActividad()).thenReturn(tablero);
		when(p3.getActividad()).thenReturn(tablero);
		
		ActividadAlumno actividadAlumno = mock(ActividadAlumno.class);
		List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();
		when(actividadAlumno.getRespuestasAlumno()).thenReturn(respuestasAlumno);
		when(actividadAlumnoService.crearActAlumno(anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong())).thenReturn(actividadAlumno);
		
		// Responder correctamente a las 3 primeras preguntas
		tableroService.crearRespuestaAPreguntaEnActTablero("resp1", 1L, 3L);
		tableroService.crearRespuestaAPreguntaEnActTablero("resp2", 1L, 4L);
		tableroService.crearRespuestaAPreguntaEnActTablero("resp3", 1L, 5L);
		
		// La 4ª respuesta completa el tablero, se debe llamar setNota()
		String result = tableroService.crearRespuestaAPreguntaEnActTablero("respuesta", 1L, 2L);
		verify(actividadAlumno).setNota(10); // 10 - (0 errores * descuento) = 10
		verify(actividadAlumno).setPuntuacion(1); // 1 - (0 errores * descuento) = 1
		assertThat(result).contains("Respuesta correcta");
	}

	@Test
	void crearRespuestaAPreguntaTablero_respVisibleFalse() {
		Alumno alumno = mock(Alumno.class);
		Pregunta pregunta = buildPreguntaWithRespuestaMaestro(2L, "respuesta");
		Tablero tablero = buildTableroWithAlumno(alumno, pregunta, true, true, false, 10, true);
		when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
		when(preguntaRepository.findById(2L)).thenReturn(Optional.of(pregunta));
		when(usuarioService.findCurrentUser()).thenReturn(alumno);
		when(pregunta.getActividad()).thenReturn(tablero);
		ActividadAlumno actividadAlumno = mock(ActividadAlumno.class);
		List<RespuestaAlumno> respuestasAlumno = new ArrayList<>();
		when(actividadAlumno.getRespuestasAlumno()).thenReturn(respuestasAlumno);
		when(actividadAlumnoService.crearActAlumno(anyInt(), any(), any(), anyInt(), anyInt(), anyLong(), anyLong())).thenReturn(actividadAlumno);
		String result = tableroService.crearRespuestaAPreguntaEnActTablero("respuesta", 1L, 2L);
		assertThat(result).isEqualTo("Respuesta correcta");
		String result2 = tableroService.crearRespuestaAPreguntaEnActTablero("incorrecta", 1L, 2L);
		assertThat(result2).isEqualTo("Respuesta incorrecta");
	}

    	// Helper to build a Tablero with all required mocks for alumno scenario
	private Tablero buildTableroWithAlumno(Alumno alumno, Pregunta pregunta, boolean cursoVisible, boolean inscrito, boolean respVisible, int puntuacion, boolean preguntaPertenece) {
		Tablero tablero = mock(Tablero.class);
		Tema tema = mock(Tema.class);
		Curso curso = mock(Curso.class);
		Inscripcion insc = mock(Inscripcion.class);
		lenient().when(tablero.getTema()).thenReturn(tema);
		lenient().when(tema.getCurso()).thenReturn(curso);
		lenient().when(curso.getVisibilidad()).thenReturn(cursoVisible);
		if (inscrito) {
			lenient().when(curso.getInscripciones()).thenReturn(List.of(insc));
			lenient().when(insc.getAlumno()).thenReturn(alumno);
		} else {
			lenient().when(curso.getInscripciones()).thenReturn(java.util.Collections.emptyList());
		}
		lenient().when(alumno.getId()).thenReturn(5L);
		if (preguntaPertenece) {
			lenient().when(tablero.getPreguntas()).thenReturn(List.of(pregunta));
		} else {
			lenient().when(tablero.getPreguntas()).thenReturn(List.of());
		}
		lenient().when(tablero.getRespVisible()).thenReturn(respVisible);
		lenient().when(tablero.getPuntuacion()).thenReturn(puntuacion);
		lenient().when(tablero.getTamano()).thenReturn(TamanoTablero.TRES_X_TRES);
		return tablero;
	}

	// Helper to build Pregunta with RespuestaMaestro
	private Pregunta buildPreguntaWithRespuestaMaestro(long id, String respuesta) {
		Pregunta pregunta = mock(Pregunta.class);
		RespuestaMaestro rm = mock(RespuestaMaestro.class);
		when(pregunta.getId()).thenReturn(id);
		when(pregunta.getRespuestasMaestro()).thenReturn(List.of(rm));
		when(rm.getRespuesta()).thenReturn(respuesta);
		return pregunta;
	}

}
