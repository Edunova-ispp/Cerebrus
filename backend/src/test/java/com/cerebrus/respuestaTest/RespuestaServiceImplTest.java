package com.cerebrus.respuestaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.organizacion.Organizacion;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.respuesta.Respuesta;
import com.cerebrus.respuesta.RespuestaRepository;
import com.cerebrus.respuesta.RespuestaServiceImpl;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class RespuestaServiceImplTest {

	@Mock
	private RespuestaRepository respuestaRepository;

	@Mock
	private PreguntaRepository preguntaRepository;

	@Mock
	private UsuarioService usuarioService;

	@InjectMocks
	private RespuestaServiceImpl respuestaService;

	// Para capturar el objeto Respuesta que se guarda en el repositorio
	@Captor
	private ArgumentCaptor<Respuesta> respuestaCaptor;

	// Test para el método crearRespuesta que verifica que se guarda correctamente una respuesta cuando el usuario 
	// es un maestro y la pregunta existe
	@Test
	void crearRespuesta_guardaRespuesta_cuandoUsuarioEsMaestro_yPreguntaExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Pregunta pregunta = new Pregunta();
		pregunta.setId(7L);
		when(preguntaRepository.findById(7L)).thenReturn(Optional.of(pregunta));
		when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(inv -> inv.getArgument(0));

		Respuesta created = respuestaService.crearRespuesta("R1", "img.png", true, 7L);

		assertThat(created).isNotNull();
		verify(usuarioService).findCurrentUser();
		verify(preguntaRepository).findById(7L);
		verify(respuestaRepository).save(respuestaCaptor.capture());
		Respuesta saved = respuestaCaptor.getValue();
		assertThat(saved.getRespuesta()).isEqualTo("R1");
		assertThat(saved.getImagen()).isEqualTo("img.png");
		assertThat(saved.getCorrecta()).isTrue();
		assertThat(saved.getPregunta()).isSameAs(pregunta);
	}

	// Test para el método crearRespuesta que verifica que se lanza una excepción de acceso denegado cuando el
	// usuario no es un maestro
	@Test
	void crearRespuesta_lanzaAccessDenied_cuandoUsuarioNoEsMaestro() {
		Usuario alumno = crearAlumno("alumno1", "a1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(alumno);

		assertThatThrownBy(() -> respuestaService.crearRespuesta("R1", null, false, 7L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("Solo un maestro puede crear respuestas");

		verify(usuarioService).findCurrentUser();
		verify(preguntaRepository, never()).findById(any());
		verify(respuestaRepository, never()).save(any());
	}

	// Test para el método crearRespuesta que verifica que se lanza una excepción ResourceNotFound cuando
	// la pregunta no existe
	@Test
	void crearRespuesta_lanzaResourceNotFound_cuandoPreguntaNoExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(preguntaRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> respuestaService.crearRespuesta("R1", "img.png", true, 999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("La pregunta de la respuesta no existe");

		verify(usuarioService).findCurrentUser();
		verify(preguntaRepository).findById(999L);
		verify(respuestaRepository, never()).save(any());
	}

	// Test para el método crearRespuesta que verifica que se permite un valor null para el atributo correcta
	@Test
	void crearRespuesta_permitaCorrectaNull() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Pregunta pregunta = new Pregunta();
		pregunta.setId(7L);
		when(preguntaRepository.findById(7L)).thenReturn(Optional.of(pregunta));
		when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(inv -> inv.getArgument(0));

		Respuesta created = respuestaService.crearRespuesta("R1", null, null, 7L);

		assertThat(created).isNotNull();
		verify(respuestaRepository).save(respuestaCaptor.capture());
		assertThat(respuestaCaptor.getValue().getCorrecta()).isNull();
	}

	// Test para el método readRespuesta que verifica que se devuelve la respuesta correcta cuando existe
	@Test
	void readRespuesta_devuelveRespuesta_cuandoExiste() {
		Respuesta expected = new Respuesta();
		expected.setId(1L);
		when(respuestaRepository.findById(1L)).thenReturn(Optional.of(expected));

		Respuesta found = respuestaService.readRespuesta(1L);

		assertThat(found).isSameAs(expected);
		verify(respuestaRepository).findById(1L);
		verify(usuarioService, never()).findCurrentUser();
	}

	// Test para el método readRespuesta que verifica que se lanza una excepción ResourceNotFound cuando la respuesta
	// no existe
	@Test
	void readRespuesta_lanzaResourceNotFound_cuandoNoExiste() {
		when(respuestaRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> respuestaService.readRespuesta(404L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("La respuesta no existe");
		verify(respuestaRepository).findById(404L);
		verify(usuarioService, never()).findCurrentUser();
	}

	// Test para el método updateRespuesta que verifica que se actualiza y guarda correctamente una respuesta cuando el
	// usuario es un maestro y la respuesta existe
	@Test
	void updateRespuesta_actualizaYGuarda_cuandoUsuarioEsMaestro_yRespuestaExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Respuesta existing = new Respuesta();
		existing.setId(5L);
		existing.setRespuesta("old");
		existing.setImagen("old.png");
		existing.setCorrecta(false);
		when(respuestaRepository.findById(5L)).thenReturn(Optional.of(existing));
		when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(inv -> inv.getArgument(0));

		Respuesta updated = respuestaService.updateRespuesta(5L, "new", null, true);

		assertThat(updated).isSameAs(existing);
		assertThat(updated.getRespuesta()).isEqualTo("new");
		assertThat(updated.getImagen()).isNull();
		assertThat(updated.getCorrecta()).isTrue();
		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository).findById(5L);
		verify(respuestaRepository).save(existing);
	}

	// Test para el método updateRespuesta que verifica que se lanza una excepción AccessDenied cuando el
	// usuario no es un maestro
	@Test
	void updateRespuesta_lanzaAccessDenied_cuandoUsuarioNoEsMaestro() {
		Usuario alumno = crearAlumno("alumno1", "a1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(alumno);

		assertThatThrownBy(() -> respuestaService.updateRespuesta(5L, "new", "img.png", true))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("Solo un maestro puede actualizar respuestas");

		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository, never()).findById(any());
		verify(respuestaRepository, never()).save(any());
	}

	// Test para el método updateRespuesta que verifica que se lanza una excepción ResourceNotFound cuando la respuesta
	// no existe
	@Test
	void updateRespuesta_lanzaResourceNotFound_cuandoRespuestaNoExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(respuestaRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> respuestaService.updateRespuesta(404L, "new", "img.png", true))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("La respuesta no existe");

		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository).findById(404L);
		verify(respuestaRepository, never()).save(any());
	}

	// Test para el método deleteRespuesta que verifica que se elimina correctamente una respuesta cuando el
	// usuario es un maestro y la respuesta existe
	@Test
	void deleteRespuesta_elimina_cuandoUsuarioEsMaestro_yRespuestaExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		Respuesta existing = new Respuesta();
		existing.setId(9L);
		when(respuestaRepository.findById(9L)).thenReturn(Optional.of(existing));

		respuestaService.deleteRespuesta(9L);

		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository).findById(9L);
		verify(respuestaRepository).delete(existing);
	}

	// Test para el método deleteRespuesta que verifica que se lanza una excepción AccessDenied cuando el
	// usuario no es un maestro
	@Test
	void deleteRespuesta_lanzaAccessDenied_cuandoUsuarioNoEsMaestro() {
		Usuario alumno = crearAlumno("alumno1", "a1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(alumno);

		assertThatThrownBy(() -> respuestaService.deleteRespuesta(9L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("Solo un maestro puede eliminar respuestas");

		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository, never()).findById(any());
		verify(respuestaRepository, never()).delete(any());
	}

	// Test para el método deleteRespuesta que verifica que se lanza una excepción ResourceNotFound cuando la respuesta
	// no existe
	@Test
	void deleteRespuesta_lanzaResourceNotFound_cuandoRespuestaNoExiste() {
		Maestro maestro = crearMaestro("maestro1", "m1@cerebrus.com");
		when(usuarioService.findCurrentUser()).thenReturn(maestro);
		when(respuestaRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> respuestaService.deleteRespuesta(404L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("La respuesta no existe");

		verify(usuarioService).findCurrentUser();
		verify(respuestaRepository).findById(404L);
		verify(respuestaRepository, never()).delete(any());
	}

	// Método auxiliar para crear un maestro de prueba
	private static Maestro crearMaestro(String username, String email) {
		return new Maestro(
				"Ana",
				"Pérez",
				"García",
				username,
				email,
				"pass",
				new Organizacion("Org"));
	}

	// Método auxiliar para crear un alumno de prueba
	private static Alumno crearAlumno(String username, String email) {
		return new Alumno(
				"Ana",
				"Pérez",
				"García",
				username,
				email,
				"pass",
				0,
				new Organizacion("Org"));
	}
}
