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
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private TemaService temaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ActividadServiceImpl actividadService;

    @Captor
    private ArgumentCaptor<Actividad> actividadCaptor;

    @Test
    void crearActividadTeoria_temaNoExiste_lanzaIllegalArgumentException() {
        when(usuarioService.findCurrentUser()).thenReturn(crearMaestro(1L));
        when(temaService.obtenerTemaPorId(99L))
                .thenThrow(new IllegalArgumentException("Tema no encontrado con ID: 99"));

        assertThatThrownBy(() -> actividadService.crearActividadTeoria("Titulo", "Desc", null, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tema no encontrado");

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActividadTeoria_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(new com.cerebrus.usuario.Usuario() {});

        assertThatThrownBy(() -> actividadService.crearActividadTeoria("T", "D", null, 1L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActividadTeoria_maxPosicionNull_asignaPosicion1_yGuardaGeneralTeoria() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(55L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(55L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(55L)).thenReturn(null);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        Actividad resultado = actividadService.crearActividadTeoria("Título", "Desc", null, 55L);

        assertThat(resultado).isNotNull();
        assertThat(resultado).isInstanceOf(General.class);

        verify(actividadRepository).save(actividadCaptor.capture());
        Actividad guardada = actividadCaptor.getValue();

        assertThat(guardada.getTitulo()).isEqualTo("Título");
        assertThat(guardada.getDescripcion()).isEqualTo("Desc");
        assertThat(guardada.getPuntuacion()).isEqualTo(0);
        assertThat(guardada.getImagen()).isNull();
        assertThat(guardada.getRespVisible()).isFalse();
        assertThat(guardada.getPosicion()).isEqualTo(1);
        assertThat(guardada.getVersion()).isEqualTo(1);
        assertThat(guardada.getTema()).isSameAs(tema);

        General general = (General) guardada;
        assertThat(general.getTipo()).isEqualTo(TipoActGeneral.TEORIA);
    }

    @Test
    void crearActividadTeoria_maxPosicion0_asignaPosicion1_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(56L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(56L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(56L)).thenReturn(0);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActividadTeoria("T", "D", null, 56L);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(1);
    }

    @Test
    void crearActividadTeoria_maxPosicionMayor_asignaMaxMasUno_yGuarda() {
        Maestro propietario = crearMaestro(7L);
        Curso curso = crearCurso(propietario);
        Tema tema = crearTema(57L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(propietario);
        when(temaService.obtenerTemaPorId(57L)).thenReturn(tema);
        when(actividadRepository.findMaxPosicionByTemaId(57L)).thenReturn(12);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        actividadService.crearActividadTeoria("T", "D", null, 57L);

        verify(actividadRepository).save(actividadCaptor.capture());
        assertThat(actividadCaptor.getValue().getPosicion()).isEqualTo(13);
    }

    @Test
    void obtenerActividadesPorTema_listaVacia_devuelveVacio() {
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        List<Actividad> resultado = actividadService.ObtenerActividadesPorTema(1L);

        assertThat(resultado).isEmpty();
        verify(actividadRepository).findByTemaId(1L);
    }

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

    private static Maestro crearMaestro(Long id) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        return maestro;
    }

    private static Curso crearCurso(Maestro maestro) {
        Curso curso = new Curso();
        curso.setMaestro(maestro);
        return curso;
    }

    private static Tema crearTema(Long id, Curso curso) {
        Tema tema = new Tema();
        tema.setId(id);
        tema.setCurso(curso);
        return tema;
    }
}