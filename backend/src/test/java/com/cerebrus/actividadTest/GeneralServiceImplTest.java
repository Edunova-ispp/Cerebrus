package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.actividad.General;
import com.cerebrus.actividad.GeneralRepository;
import com.cerebrus.actividad.GeneralServiceImpl;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class GeneralServiceImplTest {

	@Mock
	private GeneralRepository generalRepository;

	@Mock
	private TemaRepository temaRepository;

	@Mock
	private PreguntaRepository preguntaRepository;

	@Mock
	private UsuarioService usuarioService;

	@InjectMocks
	private GeneralServiceImpl generalService;

    // ArgumentCaptor para capturar el objeto General que se guarda
	@Captor
	private ArgumentCaptor<General> generalCaptor;

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro
	@Test
	void crearActGeneral_usuarioNoMaestro_lanzaAccessDeniedException() {
		Usuario usuarioNoMaestro = new Usuario() {};
		when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

		assertThatThrownBy(() -> generalService.crearActGeneral("T", "D", 10, 1L, false, null))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede crear actividades");

		verify(temaRepository, never()).findById(any());
		verify(generalRepository, never()).save(any());
	}

    // Tests para verificar que se lanza ResourceNotFoundException si el tema no existe
	@Test
	void crearActGeneral_temaNoExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.crearActGeneral("T", "D", 10, 99L, false, null))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("El tema de la actividad no existe");

		verify(generalRepository, never()).save(any());
		verify(preguntaRepository, never()).findAllById(any());
	}

    // Tests para verificar que se crea correctamente una actividad general con respVisible true, y se asigna 
    // la posición correcta según el número de actividades del tema
	@Test
	void crearActGeneral_respVisibleTrue_seteaVisibleYComentarios_versionPosicionTema() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		Tema tema = crearTema(1L);
		when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

		General general = generalService.crearActGeneral("Título", "Desc", 20, 1L, true, "ok");

		assertThat(general).isNotNull();
		assertThat(general.getTitulo()).isEqualTo("Título");
		assertThat(general.getDescripcion()).isEqualTo("Desc");
		assertThat(general.getPuntuacion()).isEqualTo(20);
		assertThat(general.getRespVisible()).isTrue();
		assertThat(general.getComentariosRespVisible()).isEqualTo("ok");
		assertThat(general.getVersion()).isEqualTo(1);
		assertThat(general.getPosicion()).isEqualTo(tema.getActividades().size());
		assertThat(general.getTema()).isSameAs(tema);
	}

    // Tests para verificar que se crea correctamente una actividad general con respVisible false, y no se 
    // guardan comentarios
	@Test
	void crearActGeneral_respVisibleFalse_noSeteaComentarios() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

		General general = generalService.crearActGeneral("T", "D", 1, 1L, false, "coment");

		assertThat(general.getRespVisible()).isFalse();
		assertThat(general.getComentariosRespVisible()).isNull();
	}

    // Tests para verificar que se lanza NullPointerException si respVisible es null, y no se llama al repositorio
	@Test
	void crearActGeneral_respVisibleNull_lanzaNullPointerException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

		assertThatThrownBy(() -> generalService.crearActGeneral("T", "D", 1, 1L, null, null))
				.isInstanceOf(NullPointerException.class);
	}

    // Tests para verificar que se crea correctamente una actividad tipo test, se asignan las preguntas y se guarda
	@Test
	void crearTipoTest_ok_seteaTipoTest_asignaPreguntas() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(10L)).thenReturn(Optional.of(crearTema(10L)));

		Pregunta p1 = new Pregunta();
		p1.setId(1L);
		Pregunta p2 = new Pregunta();
		p2.setId(2L);
		List<Long> preguntasId = List.of(1L, 2L);
		when(preguntaRepository.findAllById(preguntasId)).thenReturn(List.of(p1, p2));
		when(generalRepository.save(any(General.class))).thenAnswer(inv -> inv.getArgument(0));

		General guardada = generalService.crearTipoTest(
				"T", "D", 5, 10L, true, "c", preguntasId);

		assertThat(guardada.getTipo()).isEqualTo(TipoActGeneral.TEST);
		assertThat(guardada.getPreguntas()).containsExactly(p1, p2);
		verify(generalRepository).save(any(General.class));
		verify(preguntaRepository).findAllById(preguntasId);
	}

    // Tests para verificar que se lanza ResourceNotFoundException si el tema no existe al crear un tipo test, 
    // y no se guarda
	@Test
	void crearTipoTest_temaNoExiste_lanzaResourceNotFound() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.crearTipoTest(
				"T", "D", 1, 99L, false, null, List.of(1L)))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("El tema de la actividad no existe");

		verify(generalRepository, never()).save(any());
		verify(preguntaRepository, never()).findAllById(any());
	}

    // Tests para verificar que se lanza NullPointerException si preguntasId es null al crear un tipo test, 
    // y no se guarda
	@Test
	void crearTipoTest_preguntasIdNull_lanzaIllegalArgumentException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
		when(preguntaRepository.findAllById(any())).thenThrow(new IllegalArgumentException("preguntasId requerido"));

		assertThatThrownBy(() -> generalService.crearTipoTest(
				"T", "D", 1, 1L, false, null, null))
				.isInstanceOf(IllegalArgumentException.class);

		verify(generalRepository, never()).save(any());
	}

    // Tests para verificar que si se lee una actividad existente se devuelve correctamente
	@Test
	void readActividad_existente_devuelveActividad() {
		General general = new General();
		general.setId(5L);
		when(generalRepository.findByIdWithPreguntas(5L)).thenReturn(Optional.of(general));

		General resultado = generalService.readActividad(5L);

		assertThat(resultado).isSameAs(general);
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al leer, 
    // y no se llama al repositorio
	@Test
	void readActividad_noExiste_lanzaResourceNotFoundException() {
		when(generalRepository.findByIdWithPreguntas(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.readActividad(9L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad tipo test no encontrada");
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar,
    // y no se llama al repositorio
	@Test
	void updateActGeneral_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

		assertThatThrownBy(() -> generalService.updateActGeneral(
				1L, "T", "D", 1, false, "", 1, 1, 1L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede actualizar actividades");

		verify(generalRepository, never()).findById(any());
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al actualizar, 
    // y no se llama al repositorio para guardar
	@Test
	void updateActGeneral_actividadNoExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(generalRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.updateActGeneral(
				99L, "T", "D", 1, false, "", 1, 1, 1L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad no encontrada");
	}

    // Tests para verificar que se lanza ResourceNotFoundException si el tema no existe al actualizar, 
    // y no se llama al repositorio para guardar
	@Test
	void updateActGeneral_temaNoExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(123L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.updateActGeneral(
				1L, "T", "D", 1, false, "", 2, 3, 123L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("El tema de la actividad no existe");
	}

    // Tests para verificar que se actualiza correctamente una actividad general, se incrementa la versión, 
    // y se actualiza el tema si cambia
	@Test
	void updateActGeneral_respVisibleFalse_yComentariosBlank_dejaComentariosNull_incrementaVersion_yActualizaTema() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		general.setRespVisible(true);
		general.setComentariosRespVisible("prev");
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		Tema tema = crearTema(50L);
		when(temaRepository.findById(50L)).thenReturn(Optional.of(tema));

		General actualizada = generalService.updateActGeneral(
				1L, "Nuevo", "NuevaD", 7, false, "", 3, 10, 50L);

		assertThat(actualizada.getTitulo()).isEqualTo("Nuevo");
		assertThat(actualizada.getDescripcion()).isEqualTo("NuevaD");
		assertThat(actualizada.getPuntuacion()).isEqualTo(7);
		assertThat(actualizada.getRespVisible()).isFalse();
		assertThat(actualizada.getComentariosRespVisible()).isNull();
		assertThat(actualizada.getPosicion()).isEqualTo(3);
		assertThat(actualizada.getVersion()).isEqualTo(11);
		assertThat(actualizada.getTema()).isSameAs(tema);

		verify(generalRepository, never()).save(any());
	}

    // Tests para verificar que si se actualiza una actividad general con respVisible true, no se cambia el valor 
    // de visible y se actualizan los comentarios si no son blank
	@Test
	void updateActGeneral_respVisibleTrue_noCambiaVisible_yComentariosNoBlank_seGuardaComentarios() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		general.setRespVisible(false);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

		General actualizada = generalService.updateActGeneral(
				1L, "T", "D", 1, true, "coment", 1, 1, 1L);

		assertThat(actualizada.getRespVisible()).isFalse();
		assertThat(actualizada.getComentariosRespVisible()).isEqualTo("coment");
	}

    // Tests para verificar que se lanza NullPointerException si comentarios es null al actualizar, y no se guarda
	@Test
	void updateActGeneral_comentariosNull_lanzaNullPointerException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(generalRepository.findById(1L)).thenReturn(Optional.of(new General()));

		assertThatThrownBy(() -> generalService.updateActGeneral(
				1L, "T", "D", 1, false, null, 1, 1, 1L))
				.isInstanceOf(NullPointerException.class);
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar 
    // un tipo test, y no se llama al repositorio
	@Test
	void updateTipoTest_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

		assertThatThrownBy(() -> generalService.updateTipoTest(
				1L, "T", "D", 1, false, "", List.of(1L), 1, 1, 1L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede actualizar actividades tipo test");
	}

    // Tests para verificar que si se actualiza una actividad tipo test con preguntas null, no se llama al repositorio 
    // de preguntas y se guarda correctamente
	@Test
	void updateTipoTest_preguntasIdNull_noTocaPreguntas_yGuarda() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

		General general = new General();
		general.setId(1L);
		general.setPreguntas(new ArrayList<>(List.of(new Pregunta())));

		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
		when(generalRepository.save(any(General.class))).thenAnswer(inv -> inv.getArgument(0));

		General resultado = generalService.updateTipoTest(
				1L, "T", "D", 1, false, "", null, 2, 3, 1L);

		assertThat(resultado.getPreguntas()).hasSize(1);
		verify(preguntaRepository, never()).findAllById(any());
		verify(generalRepository).save(generalCaptor.capture());
		assertThat(generalCaptor.getValue()).isSameAs(general);
	}

    // Tests para verificar que si se actualiza una actividad tipo test con preguntasId, se reemplazan las 
    // preguntas y se guarda
	@Test
	void updateTipoTest_preguntasIdNoNull_reemplazaPreguntas_yGuarda() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

		Pregunta antigua = new Pregunta();
		antigua.setId(99L);
		General general = new General();
		general.setId(1L);
		general.setPreguntas(new ArrayList<>(List.of(antigua)));

		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

		Pregunta nueva1 = new Pregunta();
		nueva1.setId(1L);
		Pregunta nueva2 = new Pregunta();
		nueva2.setId(2L);
		List<Long> preguntasId = List.of(1L, 2L);
		when(preguntaRepository.findAllById(preguntasId)).thenReturn(List.of(nueva1, nueva2));
		when(generalRepository.save(any(General.class))).thenAnswer(inv -> inv.getArgument(0));

		General resultado = generalService.updateTipoTest(
				1L, "T", "D", 1, false, "", preguntasId, 2, 3, 1L);

		assertThat(resultado.getPreguntas()).containsExactly(nueva1, nueva2);
		verify(preguntaRepository).findAllById(preguntasId);
		verify(generalRepository).save(general);
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al eliminar, 
    // y no se llama al repositorio
	@Test
	void deleteActividad_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

		assertThatThrownBy(() -> generalService.deleteActividad(1L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede eliminar actividades");

		verify(generalRepository, never()).delete(any());
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al eliminar, 
    // y no se llama al repositorio
	@Test
	void deleteActividad_noExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(generalRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.deleteActividad(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad tipo test no encontrada");

		verify(generalRepository, never()).delete(any());
	}

    // Tests para verificar que se elimina correctamente una actividad existente
	@Test
	void deleteActividad_existente_eliminaCorrectamente() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));

		generalService.deleteActividad(1L);

		verify(generalRepository).delete(general);
	}

    // Método auxiliar para crear un objeto Maestro con id fijo
	private static Maestro crearMaestro() {
		Maestro maestro = new Maestro();
		maestro.setId(1L);
		return maestro;
	}

    // Método auxiliar para crear un objeto Tema
	private static Tema crearTema(Long id) {
		Tema tema = new Tema();
		tema.setId(id);
		return tema;
	}
}
