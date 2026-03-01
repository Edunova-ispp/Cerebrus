package com.cerebrus.actividadTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cerebrus.TipoActGeneral;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.ActividadServiceImpl;
import com.cerebrus.actividad.General;
import com.cerebrus.curso.Curso;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.Maestro;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {

	@Mock
	private ActividadRepository actividadRepository;

	@Mock
	private TemaRepository temaRepository;

	@InjectMocks
	private ActividadServiceImpl actividadService;

    // ArgumentCaptor para capturar el objeto Actividad que se guarda
	@Captor
	private ArgumentCaptor<Actividad> actividadCaptor;

    // Tests para verificar que se lanza IllegalArgumentException si el tema no existe
	@Test
	void crearActividadTeoria_temaNoExiste_lanzaIllegalArgumentException() {
		when(temaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> actividadService.crearActividadTeoria(
				"T", "D", 10, "img", 99L, 1L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Tema no encontrado");

		verify(actividadRepository, never()).findMaxPosicionByTemaId(any());
		verify(actividadRepository, never()).save(any());
	}

    // Tests para verificar que se lanza IllegalArgumentException si el maestro no es propietario del tema
	@Test
	void crearActividadTeoria_maestroNoPropietario_lanzaIllegalArgumentException() {
		Maestro propietario = crearMaestro(1L);
		Maestro noPropietario = crearMaestro(2L);
		Curso curso = crearCurso(propietario);
		Tema tema = crearTema(10L, curso);

		when(temaRepository.findById(10L)).thenReturn(Optional.of(tema));

		assertThatThrownBy(() -> actividadService.crearActividadTeoria(
				"T", "D", 10, "img", 10L, noPropietario.getId()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El maestro no es propietario del tema");

		verify(actividadRepository, never()).findMaxPosicionByTemaId(any());
		verify(actividadRepository, never()).save(any());
	}

    // Tests para verificar que se asigna posición 1 si no hay actividades previas, y se guarda la actividad correctamente
	@Test
	void crearActividadTeoria_maxPosicionNull_asignaPosicion1_yGuardaGeneralTeoria() {
		Maestro propietario = crearMaestro(7L);
		Curso curso = crearCurso(propietario);
		Tema tema = crearTema(55L, curso);

		when(temaRepository.findById(55L)).thenReturn(Optional.of(tema));
		when(actividadRepository.findMaxPosicionByTemaId(55L)).thenReturn(null);
		when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

		Actividad resultado = actividadService.crearActividadTeoria(
				"Título", "Desc", 20, null, 55L, propietario.getId());

		assertThat(resultado).isNotNull();
		assertThat(resultado).isInstanceOf(General.class);

		verify(actividadRepository).save(actividadCaptor.capture());
		Actividad guardada = actividadCaptor.getValue();

		assertThat(guardada.getTitulo()).isEqualTo("Título");
		assertThat(guardada.getDescripcion()).isEqualTo("Desc");
		assertThat(guardada.getPuntuacion()).isEqualTo(20);
		assertThat(guardada.getImagen()).isNull();
		assertThat(guardada.getRespVisible()).isFalse();
		assertThat(guardada.getPosicion()).isEqualTo(1);
		assertThat(guardada.getVersion()).isEqualTo(1);
		assertThat(guardada.getTema()).isSameAs(tema);

		General general = (General) guardada;
		assertThat(general.getTipo()).isEqualTo(TipoActGeneral.TEORIA);
	}

    // Tests para verificar que si la posición máxima es 0, se asigna posición 1 y se guarda la actividad correctamente
	@Test
	void crearActividadTeoria_maxPosicion0_asignaPosicion1_yGuarda() {
		Maestro propietario = crearMaestro(7L);
		Curso curso = crearCurso(propietario);
		Tema tema = crearTema(56L, curso);

		when(temaRepository.findById(56L)).thenReturn(Optional.of(tema));
		when(actividadRepository.findMaxPosicionByTemaId(56L)).thenReturn(0);
		when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

		actividadService.crearActividadTeoria("T", "D", 1, "img", 56L, propietario.getId());

		verify(actividadRepository).save(actividadCaptor.capture());
		assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(1);
	}

    // Tests para verificar que se asigna posición max+1 si hay actividades previas, y se guarda la actividad correctamente
	@Test
	void crearActividadTeoria_maxPosicionMayor_asignaMaxMasUno_yGuarda() {
		Maestro propietario = crearMaestro(7L);
		Curso curso = crearCurso(propietario);
		Tema tema = crearTema(57L, curso);

		when(temaRepository.findById(57L)).thenReturn(Optional.of(tema));
		when(actividadRepository.findMaxPosicionByTemaId(57L)).thenReturn(12);
		when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

		actividadService.crearActividadTeoria("T", "D", 1, "img", 57L, propietario.getId());

		verify(actividadRepository).save(actividadCaptor.capture());
		assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(13);
	}

    // Tests para verificar que se devuelve la lista vacía si no hay actividades
	@Test
	void obtenerActividadesPorTema_listaVacia_devuelveVacio() {
		when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

		List<Actividad> resultado = actividadService.ObtenerActividadesPorTema(1L);

		assertThat(resultado).isEmpty();
		verify(actividadRepository).findByTemaId(1L);
	}

    // Tests para verificar que se devuelve la lista con las actividades del tema
	@Test
	void obtenerActividadesPorTema_conElementos_devuelveMismaLista() {
		Actividad a1 = new Actividad() {};
		Actividad a2 = new Actividad() {};
		List<Actividad> lista = List.of(a1, a2);
		when(actividadRepository.findByTemaId(2L)).thenReturn(lista);

		List<Actividad> resultado = actividadService.ObtenerActividadesPorTema(2L);

		assertThat(resultado).containsExactly(a1, a2);
		verify(actividadRepository).findByTemaId(2L);
	}

    // Método auxiliar para crear objeto Maestro
	private static Maestro crearMaestro(Long id) {
		Maestro maestro = new Maestro();
		maestro.setId(id);
		return maestro;
	}

    // Método auxiliar para crear objeto Curso asociado a un Maestro
	private static Curso crearCurso(Maestro maestro) {
		Curso curso = new Curso();
		curso.setMaestro(maestro);
		return curso;
	}

    // Método auxiliar para crear objeto Tema asociado a un Curso
	private static Tema crearTema(Long id, Curso curso) {
		Tema tema = new Tema();
		tema.setId(id);
		tema.setCurso(curso);
		return tema;
	}
}
