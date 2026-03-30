package com.cerebrus.temaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.tema.TemaServiceImpl;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.maestro.MaestroRepository;

@ExtendWith(MockitoExtension.class)
class TemaServiceImplTest {

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private CursoServiceImpl cursoService;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private MaestroRepository maestroRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private TemaServiceImpl temaService;

    private Maestro maestro;
    private Maestro otroMaestro;
    private Usuario usuarioNoMaestro;
    private Curso curso;
    private Tema tema;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        maestro.setId(1L);

        otroMaestro = new Maestro();
        otroMaestro.setId(2L);

        usuarioNoMaestro = new Usuario() {};
        usuarioNoMaestro.setId(3L);

        curso = new Curso();
        curso.setId(10L);
        curso.setTitulo("Matematicas");
        curso.setMaestro(maestro);

        tema = new Tema("Fracciones", curso);
        tema.setId(100L);
    }

    @Test
    void crearTema_datosValidos_retornaTemaGuardado() {
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(temaRepository.save(any(Tema.class))).thenAnswer(inv -> inv.getArgument(0));

        Tema resultado = temaService.crearTema("Fracciones", 10L, 1L);

        assertThat(resultado.getTitulo()).isEqualTo("Fracciones");
        assertThat(resultado.getCurso()).isEqualTo(curso);
        verify(temaRepository).save(any(Tema.class));
    }

    @Test
    void crearTema_cursoNoExiste_lanzaIllegalArgumentException() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(cursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaService.crearTema("Fracciones", 999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Curso no encontrado");

        verify(temaRepository, never()).save(any());
    }

    @Test
    void crearTema_maestroNoExiste_lanzaIllegalArgumentException() {
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaService.crearTema("Fracciones", 10L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Maestro no encontrado");

        verify(temaRepository, never()).save(any());
    }

    @Test
    void crearTema_maestroNoPropietario_lanzaIllegalArgumentException() {
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(2L)).thenReturn(Optional.of(otroMaestro));

        assertThatThrownBy(() -> temaService.crearTema("Fracciones", 10L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El maestro no es propietario del curso");

        verify(temaRepository, never()).save(any());
    }

    @Test
    void renombrarTema_datosValidos_retornaTemaActualizado() {
        when(temaRepository.findById(100L)).thenReturn(Optional.of(tema));
        when(temaRepository.save(tema)).thenReturn(tema);

        Tema resultado = temaService.renombrarTema(100L, "Decimales", 1L);

        assertThat(resultado.getTitulo()).isEqualTo("Decimales");
        verify(temaRepository).save(tema);
    }

    @Test
    void renombrarTema_temaNoExiste_lanzaIllegalArgumentException() {
        when(temaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaService.renombrarTema(999L, "Decimales", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado");
    }

    @Test
    void renombrarTema_maestroNoPropietario_lanzaIllegalArgumentException() {
        when(temaRepository.findById(100L)).thenReturn(Optional.of(tema));

        assertThatThrownBy(() -> temaService.renombrarTema(100L, "Decimales", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El maestro no es propietario del tema");

        verify(temaRepository, never()).save(any());
    }

    @Test
    void encontrarTemasPorCursoAlumnoId_alumnoInscrito_retornaListaTemas() {
        when(cursoService.encontrarCursosPorUsuarioLogueado()).thenReturn(List.of(curso));
        when(temaRepository.findByCursoId(10L)).thenReturn(List.of(tema));

        List<Tema> resultado = temaService.encontrarTemasPorCursoAlumnoId(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(tema);
    }

    @Test
    void encontrarTemasPorCursoAlumnoId_alumnoNoInscrito_lanzaAccessDeniedException() {
        when(cursoService.encontrarCursosPorUsuarioLogueado()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> temaService.encontrarTemasPorCursoAlumnoId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("inscrito en este curso");

        verify(temaRepository, never()).findByCursoId(any());
    }

    @Test
    void encontrarTemasPorCursoMaestroId_usuarioEsMaestro_retornaListaTemas() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoService.encontrarCursoPorId(10L)).thenReturn(curso);
        when(temaRepository.findByCursoId(10L)).thenReturn(List.of(tema));

        List<Tema> resultado = temaService.encontrarTemasPorCursoMaestroId(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(tema);
    }

    @Test
    void encontrarTemasPorCursoMaestroId_usuarioNoEsMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        assertThatThrownBy(() -> temaService.encontrarTemasPorCursoMaestroId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("El usuario no es un maestro.");

        verify(temaRepository, never()).findByCursoId(any());
    }

    @Test
    void encontrarTemaPorId_existente_retornaTema() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(100L)).thenReturn(Optional.of(tema));

        Tema resultado = temaService.encontrarTemaPorId(100L);

        assertThat(resultado).isEqualTo(tema);
    }

    @Test
    void encontrarTemaPorId_noExiste_lanzaIllegalArgumentException() {
        when(temaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaService.encontrarTemaPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado con ID: 999");
    }

    @Test
    void eliminarTemaPorId_maestroPropietario_eliminaTemaYActividades() {
        Actividad actividad1 = new Actividad() {};
        actividad1.setId(501L);
        Actividad actividad2 = new Actividad() {};
        actividad2.setId(502L);

        when(temaRepository.findById(100L)).thenReturn(Optional.of(tema));
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findByTemaId(100L)).thenReturn(List.of(actividad1, actividad2));

        temaService.eliminarTemaPorId(100L);

        verify(actividadRepository).delete(actividad1);
        verify(actividadRepository).delete(actividad2);
        verify(temaRepository).delete(tema);
    }

    @Test
    void eliminarTemaPorId_temaNoExiste_lanzaIllegalArgumentException() {
        when(temaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaService.eliminarTemaPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado");
    }

    @Test
    void eliminarTemaPorId_usuarioSinPermiso_lanzaAccessDeniedException() {
        when(temaRepository.findById(100L)).thenReturn(Optional.of(tema));
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> temaService.eliminarTemaPorId(100L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("El usuario no tiene permiso para eliminar este tema.");

        verify(temaRepository, never()).delete(any());
    }
}
