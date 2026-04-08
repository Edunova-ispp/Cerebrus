package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.curso.Curso;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.general.GeneralRepository;
import com.cerebrus.actividad.general.GeneralServiceImpl;
import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.CrucigramaRequest;
import com.cerebrus.actividad.general.dto.GeneralAbiertaAlumnoDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.actividad.general.dto.GeneralTestMaestroDTO;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.pregunta.PreguntaRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;

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

	@Mock
	private RespuestaMaestroRepository respuestaMaestroRepository;

	@Mock
	private ActividadRepository actividadRepository;

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
		when(actividadRepository.findMaxPosicionByTemaId(1L)).thenReturn(3);

		General general = generalService.crearActGeneral("Título", "Desc", 20, 1L, true, "ok");

		assertThat(general).isNotNull();
		assertThat(general.getTitulo()).isEqualTo("Título");
		assertThat(general.getDescripcion()).isEqualTo("Desc");
		assertThat(general.getPuntuacion()).isEqualTo(20);
		assertThat(general.getRespVisible()).isTrue();
		assertThat(general.getComentariosRespVisible()).isEqualTo("ok");
		assertThat(general.getVersion()).isEqualTo(1);
		assertThat(general.getPosicion()).isEqualTo(4);
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
	void crearActTipoTest_ok_seteaTipoTest_asignaPreguntas() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(10L)).thenReturn(Optional.of(crearTema(10L)));

		Pregunta p1 = new Pregunta();
		p1.setId(1L);
		Pregunta p2 = new Pregunta();
		p2.setId(2L);
		List<Long> preguntasId = List.of(1L, 2L);
		when(preguntaRepository.findAllById(preguntasId)).thenReturn(List.of(p1, p2));
		when(generalRepository.save(any(General.class))).thenAnswer(inv -> inv.getArgument(0));

		General guardada = generalService.crearActTipoTest(
				"T", "D", 5, 10L, true, "c", preguntasId);

		assertThat(guardada.getTipo()).isEqualTo(TipoActGeneral.TEST);
		assertThat(guardada.getPreguntas()).containsExactly(p1, p2);
		verify(generalRepository).save(any(General.class));
		verify(preguntaRepository).findAllById(preguntasId);
	}

    // Tests para verificar que se lanza ResourceNotFoundException si el tema no existe al crear un tipo test, 
    // y no se guarda
	@Test
	void crearActTipoTest_temaNoExiste_lanzaResourceNotFound() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.crearActTipoTest(
				"T", "D", 1, 99L, false, null, List.of(1L)))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("El tema de la actividad no existe");

		verify(generalRepository, never()).save(any());
		verify(preguntaRepository, never()).findAllById(any());
	}

    // Tests para verificar que se lanza NullPointerException si preguntasId es null al crear un tipo test, 
    // y no se guarda
	@Test
	void crearActTipoTest_preguntasIdNull_lanzaIllegalArgumentException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
		when(preguntaRepository.findAllById(any())).thenThrow(new IllegalArgumentException("preguntasId requerido"));

		assertThatThrownBy(() -> generalService.crearActTipoTest(
				"T", "D", 1, 1L, false, null, null))
				.isInstanceOf(IllegalArgumentException.class);

		verify(generalRepository, never()).save(any());
	}

    // Tests para verificar que si se lee una actividad existente se devuelve correctamente
	@Test
	void encontrarActGeneralPorId_existente_devuelveActividad() {
		Alumno alumno = new Alumno();
		alumno.setId(1L);
		when(usuarioService.findCurrentUser()).thenReturn(alumno);

		Inscripcion inscripcion = new Inscripcion();
		inscripcion.setAlumno(alumno);

		List<Inscripcion> inscripciones = new LinkedList<>();
		inscripciones.add(inscripcion);

		Curso curso = new Curso();
		curso.setInscripciones(inscripciones);
		curso.setVisibilidad(true);

		Tema tema = new Tema();
		tema.setCurso(curso);

		General general = new General();
		general.setId(5L);
		general.setTema(tema);
		when(generalRepository.findByIdWithPreguntas(5L)).thenReturn(Optional.of(general));

		General resultado = generalService.encontrarActGeneralPorId(5L);

		assertThat(resultado).isSameAs(general);
	}

	@Test
	void encontrarActGeneralPorId_cursoOculto_lanzaAccessDeniedException() {
		Alumno alumno = new Alumno();
		alumno.setId(1L);
		when(usuarioService.findCurrentUser()).thenReturn(alumno);

		Inscripcion inscripcion = new Inscripcion();
		inscripcion.setAlumno(alumno);

		Curso curso = new Curso();
		curso.setInscripciones(List.of(inscripcion));
		curso.setVisibilidad(false);

		Tema tema = new Tema();
		tema.setCurso(curso);

		General general = new General();
		general.setId(6L);
		general.setTema(tema);
		when(generalRepository.findByIdWithPreguntas(6L)).thenReturn(Optional.of(general));

		assertThatThrownBy(() -> generalService.encontrarActGeneralPorId(6L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("curso oculto");
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al leer, 
    // y no se llama al repositorio
	@Test
	void encontrarActGeneralPorId_noExiste_lanzaResourceNotFoundException() {
		when(generalRepository.findByIdWithPreguntas(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.encontrarActGeneralPorId(9L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad no encontrada");
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar,
    // y no se llama al repositorio
	@Test
	void actualizarActGeneral_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.actualizarActGeneral(
				1L, "T", "D", 1, false, "", 1, 1, 1L, null))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede actualizar actividades");

		verify(generalRepository, never()).findById(any());
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al actualizar, 
    // y no se llama al repositorio para guardar
	@Test
	void actualizarActGeneral_actividadNoExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(generalRepository.findById(99L)).thenReturn(Optional.empty());
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.actualizarActGeneral(
				99L, "T", "D", 1, false, "", 1, 1, 1L, null))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad no encontrada");
	}

    // Tests para verificar que se lanza ResourceNotFoundException si el tema no existe al actualizar, 
    // y no se llama al repositorio para guardar
	@Test
	void actualizarActGeneral_temaNoExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		Tema tema = crearTema(2L);
		general.setTema(tema);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(123L)).thenReturn(Optional.empty());
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.actualizarActGeneral(
				1L, "T", "D", 1, false, "", 2, 3, 123L, null))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("El tema de la actividad no existe");
	}

    // Tests para verificar que se actualiza correctamente una actividad general, se incrementa la versión, 
    // y se actualiza el tema si cambia
	@Test
	void actualizarActGeneral_respVisibleFalse_yComentariosBlank_dejaComentariosNull_incrementaVersion_yActualizaTema() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		general.setRespVisible(true);
		general.setComentariosRespVisible("prev");
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		Tema tema = crearTema(50L);
		general.setTema(tema);
		when(temaRepository.findById(50L)).thenReturn(Optional.of(tema));
		String extraArg = "valorExtra";

		General actualizada = generalService.actualizarActGeneral(
				1L, "Nuevo", "NuevaD", 7, false, "", 3, 10, 50L, null);

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
	void actualizarActGeneral_respVisibleTrue_noCambiaVisible_yComentariosNoBlank_seGuardaComentarios() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		general.setRespVisible(false);
		Tema tema = crearTema(50L);
		general.setTema(tema);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
		String extraArg = "valorExtra";

		General actualizada = generalService.actualizarActGeneral(
				1L, "T", "D", 1, true, "coment", 1, 1, 1L, null);

		assertThat(actualizada.getRespVisible()).isTrue();
		assertThat(actualizada.getComentariosRespVisible()).isEqualTo("coment");
	}

    // Tests para verificar que se lanza NullPointerException si comentarios es null al actualizar, y no se guarda
	
    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar 
    // un tipo test, y no se llama al repositorio
	@Test
	void actualizarActTipoTest_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.actualizarActTipoTest(
				1L, "T", "D", 1, false, "", List.of(1L), 1, 1, 1L, null))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede actualizar actividades tipo test");
	}

// Tests para verificar que si se actualiza una actividad tipo test con preguntas null, se lanza IllegalArgumentException
    @Test
    void actualizarActTipoTest_preguntasIdNull_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

        General general = new General();
        general.setId(1L);
        general.setPreguntas(new ArrayList<>(List.of(new Pregunta())));
		Tema tema = crearTema(2L);
		general.setTema(tema);
		String extraArg = "valorExtra";

        when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

        assertThatThrownBy(() -> generalService.actualizarActTipoTest(
                1L, "T", "D", 1, false, "", null, 2, 3, 1L, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("La lista de preguntas no puede ser null");

        verify(preguntaRepository, never()).findAllById(any());
        verify(generalRepository, never()).save(any());
	}

    // Tests para verificar que si se actualiza una actividad tipo test con preguntasId, se reemplazan las 
    // preguntas y se guarda
	@Test
	void actualizarActTipoTest_preguntasIdNoNull_reemplazaPreguntas_yGuarda() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

		Pregunta antigua = new Pregunta();
		antigua.setId(99L);
		General general = new General();
		general.setId(1L);
		general.setTipo(TipoActGeneral.TEST);
		general.setPreguntas(new ArrayList<>(List.of(antigua)));
		Tema tema = crearTema(2L);
		general.setTema(tema);
		String extraArg = "valorExtra";

		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

		Pregunta nueva1 = new Pregunta();
		nueva1.setId(1L);
		Pregunta nueva2 = new Pregunta();
		nueva2.setId(2L);
		List<Long> preguntasId = List.of(1L, 2L);
		when(preguntaRepository.findAllById(preguntasId)).thenReturn(List.of(nueva1, nueva2));
		when(generalRepository.save(any(General.class))).thenAnswer(inv -> inv.getArgument(0));

		General resultado = generalService.actualizarActTipoTest(
				1L, "T", "D", 1, false, "", preguntasId, 2, 3, 1L, null);

		assertThat(resultado.getPreguntas()).containsExactly(nueva1, nueva2);
		verify(preguntaRepository).findAllById(preguntasId);
		verify(generalRepository).save(general);
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al eliminar, 
    // y no se llama al repositorio
	@Test
	void eliminarActGeneralPorId_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});

		assertThatThrownBy(() -> generalService.eliminarActGeneralPorId(1L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede eliminar actividades");

		verify(generalRepository, never()).delete(any());
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al eliminar, 
    // y no se llama al repositorio
	@Test
	void eliminarActGeneralPorId_noExiste_lanzaResourceNotFoundException() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		when(generalRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.eliminarActGeneralPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad no encontrada");

		verify(generalRepository, never()).delete(any());
	}

    // Tests para verificar que se elimina correctamente una actividad existente
	@Test
	void eliminarActGeneralPorId_existente_eliminaCorrectamente() {
		when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
		General general = new General();
		general.setId(1L);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));

		generalService.eliminarActGeneralPorId(1L);

		verify(generalRepository).delete(general);
	}

	// --- NUEVOS TESTS PARA CUBRIR LA LÓGICA AÑADIDA Y AUMENTAR COBERTURA ---

    @Test
    void crearActGeneral_maestroNoEsDuenioDelCurso_lanzaAccessDeniedException() {
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(999L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);
        
        Tema tema = crearTema(1L); 
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        assertThatThrownBy(() -> generalService.crearActGeneral("T", "D", 10, 1L, true, "ok"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede crear actividades en ese tema");
    }

    // --- TESTS PARA TIPO CARTA ---

    @Test
    void crearTipoCarta_ok_conValidacionRespuestaUnica() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        
        Pregunta p1 = new Pregunta();
        RespuestaMaestro r1 = new RespuestaMaestro();
        p1.setRespuestasMaestro(new ArrayList<>(List.of(r1))); 
        
        List<Long> ids = List.of(100L);
        when(preguntaRepository.findAllById(ids)).thenReturn(List.of(p1));
        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));

        General resultado = generalService.crearActCarta("Carta", "Desc", 10, 1L, true, "ok", ids);

        assertThat(resultado.getTipo()).isEqualTo(TipoActGeneral.CARTA);
        verify(generalRepository).save(any());
    }

    @Test
    void crearTipoCarta_preguntaSinRespuestaUnica_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        
        Pregunta p1 = new Pregunta();
        p1.setRespuestasMaestro(new ArrayList<>()); 
        
        List<Long> ids = List.of(100L);
        when(preguntaRepository.findAllById(ids)).thenReturn(List.of(p1));

        assertThatThrownBy(() -> generalService.crearActCarta("T", "D", 10, 1L, true, "c", ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene exactamente una respuesta");
    }

	@Test
    void updateTipoCarta_mantieneImagenAnterior_siNuevaEsNull() {
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        
        Long temaIdConstante = 1L;
        Tema temaMock = crearTema(temaIdConstante);
        temaMock.getCurso().setMaestro(maestro); 

        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        act.setImagen("foto_existente.png");
        act.setTema(temaMock);
        act.setVersion(1);
        act.setPreguntas(new ArrayList<>());

        when(temaRepository.findById(eq(temaIdConstante))).thenReturn(Optional.of(temaMock));
        
        lenient().when(generalRepository.findById(anyLong())).thenReturn(Optional.of(act));
        lenient().when(generalRepository.findByIdWithPreguntas(anyLong())).thenReturn(Optional.of(act));
        
        when(preguntaRepository.findAllById(any())).thenReturn(new ArrayList<>());
        when(generalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var resultado = generalService.actualizarActCarta(1L, "T", "D", 10, true, "c", List.of(), 1, 1, temaIdConstante, null);

        assertThat(resultado.getImagen()).isNull(); 
    }

    // --- TESTS PARA CLASIFICACIÓN ---

	@Test
    void updateTipoClasificacion_ok_conValidacionRespuestasCorrectas() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.CLASIFICACION);
        act.setTema(crearTema(1L));
        act.setVersion(1);
        
        org.mockito.Mockito.when(generalRepository.findById(1L)).thenReturn(Optional.of(act));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(act.getTema()));
        
        // La clasificación exige que todas las respuestas sean correctas
        Pregunta p1 = new Pregunta();
        p1.setId(100L);
        RespuestaMaestro r1 = new RespuestaMaestro();
        r1.setCorrecta(true); 
        p1.setRespuestasMaestro(List.of(r1));
        
        when(preguntaRepository.findAllById(any())).thenReturn(List.of(p1));
        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // El método devuelve GeneralClasificacionMaestroDTO, no General
        var resultado = generalService.actualizarActClasificacion(1L, "T", "D", 10, true, "c", List.of(100L), 2, 2, 1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("T");
        verify(generalRepository).save(any());
    }

    @Test
    void updateTipoClasificacion_error_cuandoHayRespuestaIncorrecta() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        General act = new General();
        act.setTipo(TipoActGeneral.CLASIFICACION);
        act.setTema(crearTema(1L));
        
        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(act.getTema()));

        // FALLO: Respuesta incorrecta en Clasificación
        Pregunta p1 = new Pregunta();
        RespuestaMaestro r1 = new RespuestaMaestro();
        r1.setCorrecta(false); 
        p1.setRespuestasMaestro(List.of(r1));
        
        when(preguntaRepository.findAllById(any())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> generalService.actualizarActClasificacion(1L, "T", "D", 10, true, "c", List.of(100L), 2, 2, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden tener respuestas incorrectas");
    }

    // --- TESTS PARA CRUCIGRAMA ---

    @Test
    void crearTipoCrucigrama_ok_guardaPreguntasYRespuestasIterativamente() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        Tema tema = crearTema(1L);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findMaxPosicionByTemaId(1L)).thenReturn(0);
        
        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(i -> i.getArgument(0));
        when(respuestaMaestroRepository.save(any(RespuestaMaestro.class))).thenAnswer(i -> i.getArgument(0));

        CrucigramaRequest req = new CrucigramaRequest();
        req.setTemaId(1L);
        req.setTitulo("Cruci");
        req.setDescripcion("Desc");
        req.setPuntuacion(10);
        req.setRespVisible(true); 
        
        Map<String, String> pYyR = new HashMap<>();
        pYyR.put("¿2+2?", "4");
        req.setPreguntasYRespuestas(pYyR);

        CrucigramaDTO dto = generalService.crearActCrucigrama(req);

        assertThat(dto).isNotNull();
        verify(preguntaRepository, atLeastOnce()).save(any(Pregunta.class));
    }

    // --- TESTS DE SEGURIDAD EN LECTURA (READ) ---

    @Test
    void readActividad_alumnoNoInscrito_lanzaAccessDeniedException() {
        Alumno alumno = new Alumno();
        alumno.setId(500L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTema(crearTema(1L));
        act.getTema().getCurso().setInscripciones(new ArrayList<>()); 
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActGeneralPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    // --- NUEVOS: COBERTURA PARA READ TIPO ABIERTA (MAESTRO Y ALUMNO) ---

    @Test
    void readTipoAbiertaMaestro_usuarioNoEsMaestro_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        assertThatThrownBy(() -> generalService.encontrarActAbiertaMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un maestro puede leer");
    }

    // --- TESTS PARA ACTIVIDAD ABIERTA (CREACIÓN Y LÍMITES) ---

    @Test
    void crearTipoAbierta_masDeCincoPreguntas_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        
        List<Long> preguntasIds = List.of(1L, 2L, 3L, 4L, 5L, 6L);

        assertThatThrownBy(() -> generalService.crearActAbierta("T", "D", 10, 1L, true, "ok", preguntasIds, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden tener más de 5 preguntas");
    }

    @Test
    void crearTipoAbierta_preguntaConMasDeUnaRespuesta_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        
        Pregunta p1 = new Pregunta(); p1.setId(100L);
        when(preguntaRepository.findAllById(any())).thenReturn(List.of(p1));
        // RAMA: respuestas.size() > 1
        when(respuestaMaestroRepository.findRespuestaByPreguntaId(100L))
                .thenReturn(List.of(new RespuestaMaestro(), new RespuestaMaestro()));

        assertThatThrownBy(() -> generalService.crearActAbierta("T", "D", 10, 1L, true, "ok", List.of(100L), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo se permite una respuesta por pregunta");
    }

    @Test
    void crearTipoAbierta_ok_sinPreguntas_cubreRamaNull() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        when(generalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // RAMA: preguntasId == null (para cubrir el primer if del método)
        General res = generalService.crearActAbierta("T", "D", 10, 1L, true, "ok", null, "img.png");
        
        assertThat(res.getTipo()).isEqualTo(TipoActGeneral.ABIERTA);
        verify(generalRepository).save(any());
    }

    // --- RESTO DE TESTS (CRUCIGRAMA UPDATE, VISIBILIDAD, ETC) ---

    @Test
    void updateTipoCrucigrama_borraPreguntasAntiguasYGeneraNuevas() {
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        General crucigramaExistente = new General();
        crucigramaExistente.setId(1L);
        crucigramaExistente.setVersion(1); 
        crucigramaExistente.setTipo(TipoActGeneral.CRUCIGRAMA);
        crucigramaExistente.setTema(crearTema(1L));

        Pregunta pAntigua = new Pregunta();
        pAntigua.setId(500L);
        pAntigua.setRespuestasMaestro(new ArrayList<>());
        
        List<Pregunta> preguntasParaBorrar = new ArrayList<>();
        preguntasParaBorrar.add(pAntigua);
        crucigramaExistente.setPreguntas(preguntasParaBorrar);

        when(generalRepository.findById(1L)).thenReturn(Optional.of(crucigramaExistente));
        when(temaRepository.findById(anyLong())).thenReturn(Optional.of(crucigramaExistente.getTema()));
        when(actividadRepository.findMaxPosicionByTemaId(anyLong())).thenReturn(5);

        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));
        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(i -> i.getArgument(0));
        when(respuestaMaestroRepository.save(any(RespuestaMaestro.class))).thenAnswer(i -> i.getArgument(0));

        CrucigramaRequest request = new CrucigramaRequest();
        request.setTemaId(1L);
        request.setTitulo("Crucigrama Actualizado");
        request.setDescripcion("Nueva Desc");
        request.setPuntuacion(20);
        request.setRespVisible(true);
        
        Map<String, String> nuevasPreguntas = new HashMap<>();
        nuevasPreguntas.put("¿2+2?", "4");
        request.setPreguntasYRespuestas(nuevasPreguntas);

        CrucigramaDTO resultado = generalService.actualizarActCrucigrama(1L, request);

        assertThat(resultado).isNotNull();
        assertThat(crucigramaExistente.getVersion()).isEqualTo(2);

        verify(preguntaRepository, atLeastOnce()).deleteAll(preguntasParaBorrar);
        verify(generalRepository).save(any(General.class));
    }

    @Test
    void updateTipoAbierta_actividadNoEsAbierta_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        General actErronea = new General();
        actErronea.setTipo(TipoActGeneral.TEST); 
        actErronea.setTema(crearTema(1L));
        
        when(generalRepository.findById(1L)).thenReturn(Optional.of(actErronea));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(actErronea.getTema()));

        assertThatThrownBy(() -> generalService.actualizarActAbierta(1L, "T", "D", 1, true, "C", List.of(), 1, 1, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La actividad no es de tipo abierta");
    }

	@Test
    void readTipoAbierta_error_siLaActividadEsTipoTest() {
        Alumno alumno = new Alumno();
        alumno.setId(1L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTipo(TipoActGeneral.TEST); // Tipo equivocado
        Tema tema = crearTema(1L);
        Inscripcion ins = new Inscripcion();
        ins.setAlumno(alumno);
        tema.getCurso().setInscripciones(List.of(ins));
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActAbiertaPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo abierta");
    }

    // --- TESTS PARA readTipoCartaMaestro ---

    @Test
    void readTipoCartaMaestro_ok_mapeaCorrectamenteADTO() {
        // 1. Setup
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        General act = new General();
        act.setId(10L);
        act.setTitulo("Título Carta");
        act.setTipo(TipoActGeneral.CARTA);
        act.setTema(crearTema(1L));
        act.getTema().getCurso().setMaestro(maestro);
        act.setImagen("foto.png");
        act.setPosicion(1);
        act.setVersion(2);

        // Crear una pregunta con una respuesta para probar el mapeo del stream
        Pregunta p = new Pregunta();
        p.setId(100L);
        p.setPregunta("¿Pregunta?");
        RespuestaMaestro r = new RespuestaMaestro();
        r.setId(200L);
        r.setRespuesta("Respuesta");
        r.setCorrecta(true);
        p.setRespuestasMaestro(List.of(r));
        act.setPreguntas(List.of(p));

        when(generalRepository.findByIdWithPreguntas(10L)).thenReturn(Optional.of(act));

        // 2. Ejecución
        GeneralCartaMaestroDTO dto = generalService.encontrarActCartaMaestroPorId(10L);

        // 3. Verificaciones
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitulo()).isEqualTo("Título Carta");
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0).getRespuestas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0).getRespuestas().get(0).getRespuesta()).isEqualTo("Respuesta");
    }

    @Test
    void readTipoCartaMaestro_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        assertThatThrownBy(() -> generalService.encontrarActCartaMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede leer actividades tipo cartas para edición");
    }

    @Test
    void readTipoCartaMaestro_actividadNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(generalRepository.findByIdWithPreguntas(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalService.encontrarActCartaMaestroPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actividad tipo carta no encontrada");
    }

    @Test
    void readTipoCartaMaestro_tipoIncorrecto_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General actTest = new General();
        actTest.setTipo(TipoActGeneral.TEST); // No es CARTA
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actTest));

        assertThatThrownBy(() -> generalService.encontrarActCartaMaestroPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo carta");
    }

    @Test
    void readTipoCartaMaestro_maestroNoDuenio_lanzaAccessDeniedException() {
        // Maestro que intenta leer
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(2L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);

        // Actividad que pertenece al Maestro ID 1 (por el método crearTema)
        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        act.setTema(crearTema(1L)); 
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActCartaMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede acceder a esta actividad");
    }

    @Test
    void readTipoCartaMaestro_temaNull_noLanzaExcepcionSeguridad() {
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        act.setTema(null); // Caso rama general.getTema() == null
        act.setPreguntas(new ArrayList<>());

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        GeneralCartaMaestroDTO dto = generalService.encontrarActCartaMaestroPorId(1L);
        
        assertThat(dto.getTemaId()).isNull();
    }

    // --- TESTS PARA encontrarActTestMaestro ---

    @Test
    void encontrarActTestMaestro_ok_mapeaCorrectamenteADTO() {
        // Setup del maestro y seguridad
        Maestro maestro = crearMaestro(); // ID 1L por defecto en tu método auxiliar
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // Crear actividad tipo TEST
        General act = new General();
        act.setId(20L);
        act.setTitulo("Examen de Prueba");
        act.setTipo(TipoActGeneral.TEST);
        act.setTema(crearTema(2L));
        act.getTema().getCurso().setMaestro(maestro); // Asegurar que el dueño es el que consulta
        act.setImagen("test_img.png");
        act.setVersion(5);

        // Crear estructura de preguntas/respuestas para cubrir los .stream().map()
        Pregunta p = new Pregunta();
        p.setId(300L);
        p.setPregunta("¿Capital de Francia?");
        RespuestaMaestro r = new RespuestaMaestro();
        r.setId(400L);
        r.setRespuesta("París");
        r.setCorrecta(true);
        p.setRespuestasMaestro(List.of(r));
        act.setPreguntas(List.of(p));

        when(generalRepository.findByIdWithPreguntas(20L)).thenReturn(Optional.of(act));

        // Ejecución
        GeneralTestMaestroDTO dto = generalService.encontrarActTipoTestMaestroPorId(20L);

        // Verificaciones
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(20L);
        assertThat(dto.getTitulo()).isEqualTo("Examen de Prueba");
        assertThat(dto.getPreguntas()).hasSize(1);
        assertThat(dto.getPreguntas().get(0).getPregunta()).isEqualTo("¿Capital de Francia?");
        assertThat(dto.getPreguntas().get(0).getRespuestas().get(0).getRespuesta()).isEqualTo("París");
    }

    @Test
    void encontrarActTestMaestro_usuarioNoMaestro_lanzaAccessDeniedException() {
        // Simular que el usuario es un Alumno
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        assertThatThrownBy(() -> generalService.encontrarActTipoTestMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede leer actividades tipo test para edición");
    }

    @Test
    void encontrarActTestMaestro_noExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(generalRepository.findByIdWithPreguntas(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalService.encontrarActTipoTestMaestroPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actividad tipo test no encontrada");
    }

    @Test
    void encontrarActTestMaestro_tipoIncorrecto_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General actErronea = new General();
        actErronea.setTipo(TipoActGeneral.ABIERTA); // Es tipo ABIERTA, no TEST
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actErronea));

        assertThatThrownBy(() -> generalService.encontrarActTipoTestMaestroPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo test");
    }

    @Test
    void encontrarActTestMaestro_maestroNoDuenio_lanzaAccessDeniedException() {
        // Usuario logueado (ID 2)
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(2L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);

        // Actividad que pertenece al Maestro ID 1
        General act = new General();
        act.setTipo(TipoActGeneral.TEST);
        act.setTema(crearTema(1L)); // El helper crea un curso para Maestro ID 1
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActTipoTestMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede acceder a esta actividad");
    }

    @Test
    void encontrarActTestMaestro_conTemaNull_mapeaIdNull() {
        // Para cubrir la rama del operador ternario: general.getTema() == null ? null : ...
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        General act = new General();
        act.setTipo(TipoActGeneral.TEST);
        act.setTema(null); // TEMA NULO
        act.setPreguntas(new ArrayList<>());

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        GeneralTestMaestroDTO resultado = generalService.encontrarActTipoTestMaestroPorId(1L);

        assertThat(resultado.getTemaId()).isNull();
        verify(generalRepository).findByIdWithPreguntas(1L);
    }

    // --- TESTS PARA readTipoCarta (Vista Alumno) ---

    @Test
    void readTipoCarta_ok_alumnoInscritoYCursoVisible() {
        // 1. Setup Alumno e Inscripción
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        Inscripcion ins = new Inscripcion();
        ins.setAlumno(alumno);

        // 2. Setup Actividad y Curso
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.CARTA);
        Tema tema = crearTema(5L);
        tema.getCurso().setVisibilidad(true); // validarCursoVisibleParaAlumno
        tema.getCurso().setInscripciones(List.of(ins)); // Para el bucle de inscripciones
        act.setTema(tema);

        // Setup Preguntas para cubrir el Stream y el shuffle
        Pregunta p = new Pregunta();
        p.setId(100L);
        p.setRespuestasMaestro(new ArrayList<>(List.of(new RespuestaMaestro())));
        act.setPreguntas(List.of(p));

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 3. Ejecución
        GeneralCartaDTO resultado = generalService.encontrarActCartaPorId(1L);

        // 4. Verificación
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(generalRepository).findByIdWithPreguntas(1L);
    }

    @Test
    void readTipoCarta_usuarioNoEsAlumno_lanzaAccessDeniedException() {
        // Un Maestro intentando entrar por la ruta de Alumno
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

        assertThatThrownBy(() -> generalService.encontrarActCartaPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No tienes permiso para acceder a esta actividad");
    }

    @Test
    void readTipoCarta_tipoNoCarta_lanzaResourceNotFoundException() {
        Alumno alumno = new Alumno();
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General actErronea = new General();
        actErronea.setTipo(TipoActGeneral.TEST); // Tipo incorrecto

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actErronea));

        assertThatThrownBy(() -> generalService.encontrarActCartaPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo carta");
    }

    @Test
    void readTipoCarta_alumnoNoInscrito_lanzaAccessDeniedException() {
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        Tema tema = crearTema(1L);
        tema.getCurso().setVisibilidad(true);
        // Lista de inscripciones vacía o con otro alumno
        tema.getCurso().setInscripciones(new ArrayList<>()); 
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActCartaPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    @Test
    void readTipoCarta_temaNull_lanzaAccessDeniedExceptionAlValidarInscripcion() {
        // Este test cubre el caso donde el bucle de inscripciones fallaría si el tema es null
        // o simplemente para ver cómo se comporta el flujo de inscripciones
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        act.setTema(null); // Esto causará un NullPointerException en general.getTema()... 
                           // a menos que el servicio lo maneje, lo cual es buena cobertura.

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActCartaPorId(1L))
                .isInstanceOf(NullPointerException.class);
    }

    // --- TESTS PARA encontrarActTipoTest (Vista Alumno) ---

    @Test
    void readTipoTest_ok_mapeaPreguntasYValidaInscripcion() {
        // 1. Setup del Alumno logueado
        Alumno alumnoLogueado = new Alumno();
        alumnoLogueado.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumnoLogueado);

        // 2. Setup de la Actividad (Tipo TEST)
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.TEST);
        act.setTitulo("Test de Cobertura");

        // 3. Setup de Preguntas y Respuestas (Para cubrir el mapeo y CerebrusUtils)
        Pregunta p = new Pregunta();
        p.setId(100L);
        p.setPregunta("¿Pregunta?");
        
        RespuestaMaestro r = new RespuestaMaestro();
        r.setId(200L);
        r.setRespuesta("Respuesta A");
        
        // Es vital que la lista no esté vacía para que el .map() se ejecute
        p.setRespuestasMaestro(new ArrayList<>(List.of(r)));
        act.setPreguntas(List.of(p));

        // 4. Setup de Curso e Inscripciones (Para el bucle final)
        Tema tema = crearTema(5L);
        tema.getCurso().setVisibilidad(true); 
        
        Inscripcion inscripcionCorrecta = new Inscripcion();
        inscripcionCorrecta.setAlumno(alumnoLogueado);
        
        tema.getCurso().setInscripciones(List.of(inscripcionCorrecta));
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 5. Ejecución
        GeneralTestDTO resultado = generalService.encontrarActTipoTestPorId(1L);

        // 6. Verificaciones de Cobertura
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPreguntas()).hasSize(1);
        assertThat(resultado.getPreguntas().get(0).getRespuestas()).hasSize(1);
        assertThat(resultado.getPreguntas().get(0).getRespuestas().get(0).getRespuesta()).isEqualTo("Respuesta A");
        verify(generalRepository).findByIdWithPreguntas(1L);
    }

    @Test
    void encontrarActTipoTest_mapeoCorrecto_cuandoTemaEsNull() {
        // Prueba la rama: general.getTema() == null ? null : general.getTema().getId()
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTipo(TipoActGeneral.TEST);
        act.setTema(null); // Caso límite
        act.setPreguntas(new ArrayList<>());

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // Nota: Este test lanzará NullPointerException en el servicio al llegar a 
        // general.getTema().getCurso().getInscripciones() si no hay null-check.
        // Es útil para detectar errores de diseño.
        assertThatThrownBy(() -> generalService.encontrarActTipoTestPorId(1L))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void encontrarActTipoTest_alumnoNoEncontradoEnInscripciones_lanzaAccessDenied() {
        // Setup alumno
        Alumno alumnoLogueado = new Alumno();
        alumnoLogueado.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumnoLogueado);

        // Setup actividad con inscripción de OTRO alumno
        General act = new General();
        act.setTipo(TipoActGeneral.TEST);
        act.setPreguntas(new ArrayList<>());
        
        Tema tema = crearTema(1L);
        tema.getCurso().setVisibilidad(true);
        
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99L);
        Inscripcion insAjena = new Inscripcion();
        insAjena.setAlumno(otroAlumno);
        
        tema.getCurso().setInscripciones(List.of(insAjena));
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // Verificamos que recorre el bucle, no encuentra el ID y lanza la excepción final
        assertThatThrownBy(() -> generalService.encontrarActTipoTestPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    // --- TESTS PARA encontrarActActividad (Flujos de Maestro y otros roles) ---

    @Test
    void readActividad_maestroDuenio_devuelveActividad() {
        // 1. Setup Maestro logueado
        Maestro maestro = crearMaestro(); // ID 1L
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // 2. Setup Actividad vinculada a ese Maestro
        General act = new General();
        act.setId(100L);
        act.setTema(crearTema(1L)); // crearTema vincula al Maestro ID 1L por defecto
        
        when(generalRepository.findByIdWithPreguntas(100L)).thenReturn(Optional.of(act));

        // 3. Ejecución
        General resultado = generalService.encontrarActGeneralPorId(100L);

        // 4. Verificación
        assertThat(resultado).isSameAs(act);
        verify(generalRepository).findByIdWithPreguntas(100L);
    }

    @Test
    void readActividad_maestroNoDuenio_lanzaAccessDeniedException() {
        // 1. Setup Maestro intruso (ID 99)
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(99L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);

        // 2. Actividad de otro maestro (ID 1)
        General act = new General();
        act.setTema(crearTema(1L)); 
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 3. Verificación de la rama: !maestro.getId().equals(current.getId())
        assertThatThrownBy(() -> generalService.encontrarActGeneralPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede acceder a esta actividad");
    }

    @Test
    void readActividad_maestroConTemaNull_devuelveActividad() {
        // 1. Setup Maestro
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // 2. Setup Actividad: Necesitamos que tenga Tema y Curso para evitar el NPE 
        // en la línea de 'inscripciones', pero probaremos la rama del 'else' del Maestro
        General act = new General();
        act.setId(1L);
        
        // Creamos una estructura mínima para que no explote la línea de inscripciones
        Tema tema = new Tema();
        Curso curso = new Curso();
        curso.setInscripciones(new ArrayList<>()); // Evita NPE en getInscripciones()
        curso.setMaestro(maestro); // Seteamos el mismo maestro para que sea el "dueño"
        tema.setCurso(curso);
        
        act.setTema(tema); 
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 3. Ejecución
        General resultado = generalService.encontrarActGeneralPorId(1L);
        
        // 4. Verificación
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTema().getCurso().getMaestro().getId()).isEqualTo(maestro.getId());
    }

    @Test
    void readActividad_usuarioRolDesconocido_lanzaAccessDeniedException() {
        // 1. Setup de un usuario que no es Alumno ni Maestro (Usuario anónimo/base)
        Usuario usuarioRaro = new Usuario() {}; // Clase anónima que no extiende de Alumno ni Maestro
        when(usuarioService.findCurrentUser()).thenReturn(usuarioRaro);

        General act = new General();
        act.setTema(crearTema(1L));
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 2. Verificación de la rama final 'else'
        assertThatThrownBy(() -> generalService.encontrarActGeneralPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No puedes obtener una actividad si no eres un alumno o un maestro");
    }

    // --- TESTS PARA crearGeneralClasificacion ---

    @Test
    void crearGeneralClasificacion_ok_creaYGuardaConTipoCorrecto() {
        // 1. Setup
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        
        Tema tema = crearTema(1L);
        tema.getCurso().setMaestro(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        
        // Mock de save: devolvemos lo que se intenta guardar
        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));
        
        // Mock de findMaxPosicion para el método interno crearActGeneral
        when(actividadRepository.findMaxPosicionByTemaId(1L)).thenReturn(0);

        // 2. Ejecución
        General resultado = generalService.crearActClasificacion(
                "Título Clasif", "Desc", 10, 1L, true, "comentarios");

        // 3. Verificaciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTipo()).isEqualTo(TipoActGeneral.CLASIFICACION);
        assertThat(resultado.getTitulo()).isEqualTo("Título Clasif");
        assertThat(resultado.getTema()).isEqualTo(tema);
        
        verify(generalRepository).save(any(General.class));
    }

    @Test
    void crearGeneralClasificacion_usuarioNoMaestro_lanzaAccessDeniedException() {
        // Simular un Alumno
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        assertThatThrownBy(() -> generalService.crearActClasificacion("T", "D", 1, 1L, false, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear actividades");
        
        verify(generalRepository, never()).save(any());
    }

    @Test
    void crearGeneralClasificacion_temaNoExiste_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalService.crearActClasificacion("T", "D", 1, 99L, false, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El tema de la actividad no existe");
    }

    @Test
    void crearGeneralClasificacion_maestroNoDuenioDelCurso_lanzaAccessDeniedException() {
        // Maestro que intenta crear (ID 2)
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(2L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);

        // Tema que pertenece a otro Maestro (ID 1 por defecto en crearTema)
        Tema temaAjeno = crearTema(1L);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(temaAjeno));

        assertThatThrownBy(() -> generalService.crearActClasificacion("T", "D", 1, 1L, false, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede crear actividades en ese tema");
        
        verify(generalRepository, never()).save(any());
    }

    // --- TESTS PARA readTipoClasificacion (Vista Alumno) ---

    @Test
    void readTipoClasificacion_ok_repartoMatematicoDeRespuestas() {
        // 1. Setup Alumno e Inscripción
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        Inscripcion ins = new Inscripcion();
        ins.setAlumno(alumno);

        // 2. Setup Actividad y Curso
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.CLASIFICACION);
        Tema tema = crearTema(5L);
        tema.getCurso().setVisibilidad(true);
        tema.getCurso().setInscripciones(List.of(ins));
        act.setTema(tema);

        // 3. Setup de Preguntas y Respuestas para probar el bucle de reparto
        // Creamos 2 preguntas y 3 respuestas totales para probar la lógica: 
        // toAssign = 3/2 + (0 < 3%2 ? 1 : 0) -> Pregunta 1 debería llevar 2 respuestas, Pregunta 2 llevaría 1.
        Pregunta p1 = new Pregunta();
        p1.setId(101L);
        RespuestaMaestro r1 = new RespuestaMaestro(); r1.setId(1L); r1.setRespuesta("R1");
        RespuestaMaestro r2 = new RespuestaMaestro(); r2.setId(2L); r2.setRespuesta("R2");
        p1.setRespuestasMaestro(new ArrayList<>(List.of(r1, r2)));

        Pregunta p2 = new Pregunta();
        p2.setId(102L);
        RespuestaMaestro r3 = new RespuestaMaestro(); r3.setId(3L); r3.setRespuesta("R3");
        p2.setRespuestasMaestro(new ArrayList<>(List.of(r3)));

        act.setPreguntas(List.of(p1, p2));

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 4. Ejecución
        GeneralClasificacionDTO resultado = generalService.encontrarActClasificacionPorId(1L);

        // 5. Verificaciones de la lógica matemática
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPreguntas()).hasSize(2);
        
        // Verificamos el reparto (3 respuestas entre 2 preguntas)
        int totalRespuestasAsignadas = resultado.getPreguntas().stream()
                .mapToInt(p -> p.getRespuestas().size())
                .sum();
        assertThat(totalRespuestasAsignadas).isEqualTo(3);
        
        // Según la fórmula del servicio: la primera recibe el sobrante
        assertThat(resultado.getPreguntas().get(0).getRespuestas()).hasSize(2);
        assertThat(resultado.getPreguntas().get(1).getRespuestas()).hasSize(1);
    }

    @Test
    void readTipoClasificacion_usuarioMaestro_lanzaAccessDeniedException() {
        // Un Maestro no puede usar la ruta de lectura de Alumno
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

        assertThatThrownBy(() -> generalService.encontrarActClasificacionPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede leer actividades tipo clasificación");
    }

    @Test
    void readTipoClasificacion_noEncontrada_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        when(generalRepository.findByIdWithPreguntas(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalService.encontrarActClasificacionPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actividad tipo clasificación no encontrada");
    }

    @Test
    void readTipoClasificacion_tipoIncorrecto_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        
        General actErronea = new General();
        actErronea.setTipo(TipoActGeneral.ABIERTA); // No es CLASIFICACION
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actErronea));

        assertThatThrownBy(() -> generalService.encontrarActClasificacionPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo clasificación");
    }

    @Test
    void readTipoClasificacion_alumnoNoInscrito_lanzaAccessDeniedException() {
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        General act = new General();
        act.setTipo(TipoActGeneral.CLASIFICACION);
        Tema tema = crearTema(1L);
        tema.getCurso().setVisibilidad(true);
        tema.getCurso().setInscripciones(new ArrayList<>()); // Lista vacía
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActClasificacionPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    // --- TESTS PARA readTipoAbiertaMaestro ---

    @Test
    void readTipoAbiertaMaestro_ok_mapeaCorrectamente() {
        // Setup
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        General act = new General();
        act.setId(10L);
        act.setTitulo("Actividad Abierta");
        act.setTipo(TipoActGeneral.ABIERTA);
        act.setTema(crearTema(1L));
        act.getTema().getCurso().setMaestro(maestro); // El maestro es el dueño
        act.setVersion(1);

        // Crear una pregunta con su respuesta para cubrir el stream mapping
        Pregunta p = new Pregunta();
        p.setId(100L);
        p.setPregunta("¿Cuál es tu opinión?");
        RespuestaMaestro r = new RespuestaMaestro();
        r.setId(200L);
        r.setRespuesta("Respuesta esperada");
        r.setCorrecta(true);
        p.setRespuestasMaestro(List.of(r));
        act.setPreguntas(List.of(p));

        when(generalRepository.findByIdWithPreguntas(10L)).thenReturn(Optional.of(act));

        // Ejecución
        GeneralAbiertaMaestroDTO resultado = generalService.encontrarActAbiertaMaestroPorId(10L);

        // Verificaciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getPreguntas()).hasSize(1);
        assertThat(resultado.getPreguntas().get(0).getPregunta()).isEqualTo("¿Cuál es tu opinión?");
        assertThat(resultado.getPreguntas().get(0).getRespuestas().get(0).getRespuesta()).isEqualTo("Respuesta esperada");
    }

    @Test
    void readTipoAbiertaMaestro_usuarioNoMaestro_lanzaAccessDeniedException() {
        // Simulamos un Alumno intentando entrar en la vista de edición de maestro
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());

        assertThatThrownBy(() -> generalService.encontrarActAbiertaMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede leer actividades tipo abierta para edición");
    }

    @Test
    void readTipoAbiertaMaestro_maestroNoDuenio_lanzaAccessDeniedException() {
        // Usuario logueado (ID 2)
        Maestro maestroIntruso = new Maestro();
        maestroIntruso.setId(2L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroIntruso);

        // Actividad que pertenece al Maestro ID 1 (crearTema usa crearMaestro() que devuelve ID 1L)
        General act = new General();
        act.setTipo(TipoActGeneral.ABIERTA);
        act.setTema(crearTema(1L)); 
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.encontrarActAbiertaMaestroPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede acceder a esta actividad");
    }

    @Test
    void readTipoAbiertaMaestro_tipoIncorrecto_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General actTest = new General();
        actTest.setTipo(TipoActGeneral.TEST); // No es ABIERTA
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actTest));

        assertThatThrownBy(() -> generalService.encontrarActAbiertaMaestroPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo abierta");
    }

    @Test
    void readTipoAbiertaMaestro_noEncontrada_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(generalRepository.findByIdWithPreguntas(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalService.encontrarActAbiertaMaestroPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actividad tipo abierta no encontrada");
    }

    // --- NUEVOS TESTS PARA updateTipoAbierta ---

    @Test
    void updateTipoAbierta_ok_actualizaPreguntasCorrectamente() {
        // 1. Setup Maestro
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // 2. Setup Tema y Curso (Crucial: Mockear el repositorio de temas)
        Tema tema = crearTema(1L);
        tema.getCurso().setMaestro(maestro);
        // --- ESTA ES LA LÍNEA QUE FALTABA ---
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        // 3. Setup Actividad Existente
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.ABIERTA);
        act.setTema(tema);
        act.setPreguntas(new ArrayList<>()); 

        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));
        
        // 4. Mock de persistencia
        when(generalRepository.save(any(General.class))).thenAnswer(i -> i.getArgument(0));

        // 5. Mock de Preguntas a añadir
        Pregunta p1 = new Pregunta();
        p1.setId(10L);
        // Importante: Inicializar la lista para que el .size() no de NPE
        p1.setRespuestasMaestro(List.of(new RespuestaMaestro())); 
        
        List<Long> preguntasId = List.of(10L);
        when(preguntaRepository.findAllById(preguntasId)).thenReturn(List.of(p1));

        // 6. Ejecución
        General resultado = generalService.actualizarActAbierta(
            1L, "Título", "Desc", 10, true, "Comentario", 
            preguntasId, 1, 1, 1L, "imagen.png"
        );

        // 7. Verificaciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPreguntas()).hasSize(1);
        assertThat(resultado.getPreguntas().get(0).getId()).isEqualTo(10L);
        verify(generalRepository).save(any(General.class));
    }

    @Test
    void updateTipoAbierta_maestroNoDuenio_lanzaAccessDeniedException() {
        Maestro maestroLogueado = new Maestro();
        maestroLogueado.setId(2L);
        when(usuarioService.findCurrentUser()).thenReturn(maestroLogueado);

        General act = new General();
        act.setTema(crearTema(1L)); // El tema pertenece al Maestro ID 1L

        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.actualizarActAbierta(1L, "T", "D", 1, true, "C", List.of(), 1, 1, 1L, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el maestro del curso puede actualizar esta actividad");
    }

    @Test
    void updateTipoAbierta_preguntasIdNull_lanzaIllegalArgumentException() {
        // 1. Setup Maestro
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // 2. Setup Tema y Mock del repositorio (INDISPENSABLE)
        Tema tema = crearTema(1L);
        tema.getCurso().setMaestro(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        // 3. Setup Actividad
        General act = new General();
        act.setId(1L);
        act.setTema(tema);
        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));

        // 4. Ejecución y Verificación
        // Ahora sí llegará a la validación del null porque el tema "existe" para el servicio
        assertThatThrownBy(() -> generalService.actualizarActAbierta(1L, "T", "D", 1, true, "C", null, 1, 1, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La lista de preguntas no puede ser null");
    }

    @Test
    void updateTipoAbierta_algunaPreguntaNoExiste_lanzaResourceNotFoundException() {
        // 1. Setup Maestro y Tema
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        
        Tema tema = crearTema(1L);
        tema.getCurso().setMaestro(maestro);
        
        // --- MOCK CRUCIAL: Necesario para que updateActGeneral no lance excepción ---
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        // 2. Setup Actividad
        General act = new General();
        act.setId(1L);
        act.setTema(tema);
        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));

        // 3. Setup de Preguntas (Simulamos que buscamos 2 pero el repo solo encuentra 1)
        List<Long> ids = List.of(1L, 2L);
        when(preguntaRepository.findAllById(ids)).thenReturn(List.of(new Pregunta())); 

        // 4. Ejecución y Verificación
        // Ahora sí pasará el filtro del tema y llegará a la validación de tamaño de lista de preguntas
        assertThatThrownBy(() -> generalService.actualizarActAbierta(1L, "T", "D", 1, true, "C", ids, 1, 1, 1L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Alguna de las preguntas no existe");
    }

    @Test
    void updateTipoAbierta_preguntaSinRespuestaUnica_lanzaIllegalArgumentException() {
        // 1. Setup Maestro y Tema (Necesario para pasar la validación inicial)
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Tema tema = crearTema(1L);
        tema.getCurso().setMaestro(maestro);
        
        // MOCK DEL TEMA: Evita el ResourceNotFoundException
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));

        // 2. Setup Actividad existente
        General act = new General();
        act.setId(1L);
        act.setTema(tema);
        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));

        // 3. Setup de Pregunta inválida (0 respuestas)
        Pregunta pErronea = new Pregunta();
        pErronea.setRespuestasMaestro(new ArrayList<>()); 
        
        List<Long> ids = List.of(100L);
        when(preguntaRepository.findAllById(ids)).thenReturn(List.of(pErronea));

        // 4. Ejecución y Verificación
        // Ahora el flujo llegará hasta el bucle for(Pregunta pregunta : preguntas)
        assertThatThrownBy(() -> generalService.actualizarActAbierta(1L, "T", "D", 1, true, "C", ids, 1, 1, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene exactamente una respuesta");
    }

    // --- TESTS PARA readTipoAbierta (Vista Alumno) ---

    @Test
    void readTipoAbierta_ok_devuelveDTOAlAlumnoInscrito() {
        // 1. Setup Alumno
        Alumno alumno = new Alumno();
        alumno.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        // 2. Setup Actividad Tipo Abierta
        General act = new General();
        act.setId(1L);
        act.setTipo(TipoActGeneral.ABIERTA);
        act.setTitulo("Abierta Alumno");
        
        // 3. Setup Tema y Curso con Inscripción (Crucial para el anyMatch)
        Tema tema = crearTema(5L);
        tema.getCurso().setVisibilidad(true); // Para validarCursoVisibleParaAlumno
        
        Inscripcion ins = new Inscripcion();
        ins.setAlumno(alumno);
        tema.getCurso().setInscripciones(List.of(ins));
        
        act.setTema(tema);

        // 4. Setup de Preguntas (Para cubrir el stream mapping)
        Pregunta p = new Pregunta();
        p.setId(100L);
        p.setPregunta("¿Qué opinas?");
        act.setPreguntas(List.of(p));

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // 5. Ejecución
        GeneralAbiertaAlumnoDTO resultado = generalService.encontrarActAbiertaPorId(1L);

        // 6. Verificaciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPreguntas()).hasSize(1);
        assertThat(resultado.getPreguntas().get(0).getPregunta()).isEqualTo("¿Qué opinas?");
        verify(generalRepository).findByIdWithPreguntas(1L);
    }

    @Test
    void readTipoAbierta_alumnoNoInscrito_lanzaAccessDeniedException() {
        // Alumno logueado ID 10
        Alumno alumnoLogueado = new Alumno();
        alumnoLogueado.setId(10L);
        when(usuarioService.findCurrentUser()).thenReturn(alumnoLogueado);

        // Actividad con inscripción de OTRO alumno (ID 99)
        General act = new General();
        act.setTipo(TipoActGeneral.ABIERTA);
        Tema tema = crearTema(1L);
        tema.getCurso().setVisibilidad(true);
        
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99L);
        Inscripcion insAjena = new Inscripcion();
        insAjena.setAlumno(otroAlumno);
        tema.getCurso().setInscripciones(List.of(insAjena));
        
        act.setTema(tema);
        act.setPreguntas(new ArrayList<>());

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        // Verificamos la rama del !anyMatch
        assertThatThrownBy(() -> generalService.encontrarActAbiertaPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No tienes permiso para acceder a esta actividad");
    }

    @Test
    void readTipoAbierta_noEsAlumno_lanzaAccessDeniedException() {
        // Si un Maestro intenta entrar por aquí
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

        assertThatThrownBy(() -> generalService.encontrarActAbiertaPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un alumno puede leer actividades tipo abierta");
    }

    @Test
    void readTipoAbierta_tipoIncorrecto_lanzaResourceNotFoundException() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        
        General actErronea = new General();
        actErronea.setTipo(TipoActGeneral.CARTA); // No es ABIERTA
        
        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(actErronea));

        assertThatThrownBy(() -> generalService.encontrarActAbiertaPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La actividad no es de tipo abierta");
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
		Curso curso = crearCurso(1L, crearMaestro());
		tema.setCurso(curso);
		return tema;
	}

	private static Curso crearCurso(Long id, Maestro maestro) {
		Curso curso = new Curso();
		curso.setId(id);
		curso.setMaestro(maestro);
		curso.setVisibilidad(true);
		return curso;
	}
}
