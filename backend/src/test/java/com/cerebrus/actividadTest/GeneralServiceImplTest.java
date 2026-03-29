package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

		General resultado = generalService.readActividad(5L);

		assertThat(resultado).isSameAs(general);
	}

	@Test
	void readActividad_cursoOculto_lanzaAccessDeniedException() {
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

		assertThatThrownBy(() -> generalService.readActividad(6L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("curso oculto");
	}

    // Tests para verificar que se lanza ResourceNotFoundException si la actividad no existe al leer, 
    // y no se llama al repositorio
	@Test
	void readActividad_noExiste_lanzaResourceNotFoundException() {
		when(generalRepository.findByIdWithPreguntas(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> generalService.readActividad(9L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Actividad no encontrada");
	}

    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar,
    // y no se llama al repositorio
	@Test
	void updateActGeneral_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.updateActGeneral(
				1L, "T", "D", 1, false, "", 1, 1, 1L, extraArg))
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
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.updateActGeneral(
				99L, "T", "D", 1, false, "", 1, 1, 1L, extraArg))
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
		Tema tema = crearTema(2L);
		general.setTema(tema);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(123L)).thenReturn(Optional.empty());
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.updateActGeneral(
				1L, "T", "D", 1, false, "", 2, 3, 123L, extraArg))
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
		general.setTema(tema);
		when(temaRepository.findById(50L)).thenReturn(Optional.of(tema));
		String extraArg = "valorExtra";

		General actualizada = generalService.updateActGeneral(
				1L, "Nuevo", "NuevaD", 7, false, "", 3, 10, 50L, extraArg);

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
		Tema tema = crearTema(50L);
		general.setTema(tema);
		when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
		when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
		String extraArg = "valorExtra";

		General actualizada = generalService.updateActGeneral(
				1L, "T", "D", 1, true, "coment", 1, 1, 1L, extraArg);

		assertThat(actualizada.getRespVisible()).isTrue();
		assertThat(actualizada.getComentariosRespVisible()).isEqualTo("coment");
	}

    // Tests para verificar que se lanza NullPointerException si comentarios es null al actualizar, y no se guarda
	
    // Tests para verificar que se lanza AccessDeniedException si el usuario actual no es un maestro al actualizar 
    // un tipo test, y no se llama al repositorio
	@Test
	void updateTipoTest_usuarioNoMaestro_lanzaAccessDeniedException() {
		when(usuarioService.findCurrentUser()).thenReturn(new Usuario() {});
		String extraArg = "valorExtra";

		assertThatThrownBy(() -> generalService.updateTipoTest(
				1L, "T", "D", 1, false, "", List.of(1L), 1, 1, 1L, extraArg))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Solo un maestro puede actualizar actividades tipo test");
	}

// Tests para verificar que si se actualiza una actividad tipo test con preguntas null, se lanza IllegalArgumentException
    @Test
    void updateTipoTest_preguntasIdNull_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());

        General general = new General();
        general.setId(1L);
        general.setPreguntas(new ArrayList<>(List.of(new Pregunta())));
		Tema tema = crearTema(2L);
		general.setTema(tema);
		String extraArg = "valorExtra";

        when(generalRepository.findById(1L)).thenReturn(Optional.of(general));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));

        assertThatThrownBy(() -> generalService.updateTipoTest(
                1L, "T", "D", 1, false, "", null, 2, 3, 1L, extraArg))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("La lista de preguntas no puede ser null");

        verify(preguntaRepository, never()).findAllById(any());
        verify(generalRepository, never()).save(any());
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

		General resultado = generalService.updateTipoTest(
				1L, "T", "D", 1, false, "", preguntasId, 2, 3, 1L, extraArg);

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
				.hasMessage("Actividad no encontrada");

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

        General resultado = generalService.crearTipoCarta("Carta", "Desc", 10, 1L, true, "ok", ids);

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

        assertThatThrownBy(() -> generalService.crearTipoCarta("T", "D", 10, 1L, true, "c", ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene exactamente una respuesta");
    }

	/*@Test
    void updateTipoCarta_mantieneImagenAnterior_siNuevaEsNull() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General act = new General();
        act.setTipo(TipoActGeneral.CARTA);
        act.setImagen("foto_existente.png");
        act.setTema(crearTema(1L));
        act.setVersion(1);
        act.setPreguntas(new ArrayList<>());
        
		lenient().when(temaRepository.findById(anyLong())).thenReturn(Optional.of(act.getTema()));
		lenient().when(generalRepository.findById(anyLong())).thenReturn(Optional.of(act));
		lenient().when(generalRepository.findByIdWithPreguntas(anyLong())).thenReturn(Optional.of(act));
        when(preguntaRepository.findAllById(any())).thenReturn(new ArrayList<>());
        when(generalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Enviamos null en el último parámetro (imagen)
        var resultado = generalService.updateTipoCarta(1L, "T", "D", 10, true, "c", List.of(), 1, 1, 1L, null);

        // Verificamos que no se borró la imagen que ya tenía
        assertThat(act.getImagen()).isEqualTo("foto_existente.png");
    }*/

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
        var resultado = generalService.updateTipoClasificacion(1L, "T", "D", 10, true, "c", List.of(100L), 2, 2, 1L);

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

        assertThatThrownBy(() -> generalService.updateTipoClasificacion(1L, "T", "D", 10, true, "c", List.of(100L), 2, 2, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden tener respuestas incorrectas");
    }

    @Test
    void updateTipoClasificacion_conRespuestaIncorrecta_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        
        General act = new General();
        act.setTema(crearTema(1L));
        act.setPreguntas(new ArrayList<>());
        when(generalRepository.findById(1L)).thenReturn(Optional.of(act));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(act.getTema()));

        Pregunta p1 = new Pregunta();
        RespuestaMaestro r1 = new RespuestaMaestro();
        r1.setCorrecta(false); 
        p1.setRespuestasMaestro(List.of(r1));

        List<Long> ids = List.of(100L);
        when(preguntaRepository.findAllById(ids)).thenReturn(List.of(p1));

        assertThatThrownBy(() -> generalService.updateTipoClasificacion(1L, "T", "D", 10, true, "c", ids, 1, 1, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Las preguntas de una actividad de clasificación no pueden tener respuestas incorrectas");
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

        CrucigramaDTO dto = generalService.crearTipoCrucigrama(req);

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

        assertThatThrownBy(() -> generalService.readActividad(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La actividad que buscas pertenece a un curso al que no estás inscrito");
    }

    // --- NUEVOS: COBERTURA PARA READ TIPO ABIERTA (MAESTRO Y ALUMNO) ---

    @Test
    void readTipoAbiertaMaestro_usuarioNoEsMaestro_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(new Alumno());
        assertThatThrownBy(() -> generalService.readTipoAbiertaMaestro(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo un maestro puede leer");
    }

    @Test
    void readTipoAbierta_alumnoNoInscrito_lanzaAccessDenied() {
        Alumno alumno = new Alumno(); alumno.setId(99L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        General act = new General();
        act.setTipo(TipoActGeneral.ABIERTA);
        Tema tema = crearTema(1L);
        tema.getCurso().setInscripciones(new ArrayList<>()); // No está el alumno 99
        act.setTema(tema);

        when(generalRepository.findByIdWithPreguntas(1L)).thenReturn(Optional.of(act));

        assertThatThrownBy(() -> generalService.readTipoAbierta(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No tienes permiso para acceder");
    }

    // --- TESTS PARA ACTIVIDAD ABIERTA (CREACIÓN Y LÍMITES) ---

    @Test
    void crearTipoAbierta_masDeCincoPreguntas_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        
        List<Long> preguntasIds = List.of(1L, 2L, 3L, 4L, 5L, 6L);

        assertThatThrownBy(() -> generalService.crearTipoAbierta("T", "D", 10, 1L, true, "ok", preguntasIds, null))
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

        assertThatThrownBy(() -> generalService.crearTipoAbierta("T", "D", 10, 1L, true, "ok", List.of(100L), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo se permite una respuesta por pregunta");
    }

    @Test
    void crearTipoAbierta_ok_sinPreguntas_cubreRamaNull() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro());
        when(temaRepository.findById(1L)).thenReturn(Optional.of(crearTema(1L)));
        when(generalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // RAMA: preguntasId == null (para cubrir el primer if del método)
        General res = generalService.crearTipoAbierta("T", "D", 10, 1L, true, "ok", null, "img.png");
        
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

        CrucigramaDTO resultado = generalService.updateTipoCrucigrama(1L, request);

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

        assertThatThrownBy(() -> generalService.updateTipoAbierta(1L, "T", "D", 1, true, "C", List.of(), 1, 1, 1L, null))
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

        assertThatThrownBy(() -> generalService.readTipoAbierta(1L))
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
