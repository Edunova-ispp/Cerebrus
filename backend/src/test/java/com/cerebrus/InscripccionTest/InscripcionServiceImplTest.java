package com.cerebrus.InscripccionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.inscripcion.InscripcionRepository;
import com.cerebrus.inscripcion.InscripcionServiceImpl;
import com.cerebrus.inscripcion.dto.AlumnoCursoDTO;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    private Alumno alumno;
    private Maestro maestro;
    private Curso curso;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);
        alumno.setNombre("Alumno 1");

        maestro = new Maestro();
        maestro.setId(2L);

        curso = new Curso();
        curso.setId(10L);
        curso.setCodigo("ABC123");
        curso.setVisibilidad(true);
    }

    // ==================== CrearInscripcion ====================

    @Test
    void crearInscripcion_alumnoYCursoVisibleSinInscripcionPrevia_creaYRetornaInscripcion() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("ABC123")).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);
        when(inscripcionRepository.findByAlumnoIdAndCursoId(1L, 10L)).thenReturn(null);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        Inscripcion resultado = inscripcionService.crearInscripcion("ABC123");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAlumno()).isEqualTo(alumno);
        assertThat(resultado.getCurso()).isEqualTo(curso);
        assertThat(resultado.getPuntos()).isEqualTo(0);
        assertThat(resultado.getFechaInscripcion()).isNotNull();
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_cursoNoExiste_lanzaRuntimeException404() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("NOPE")).thenReturn(false);

        assertThatThrownBy(() -> inscripcionService.crearInscripcion("NOPE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_cursoNoVisible_lanzaRuntimeException403() {
        curso.setVisibilidad(false);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("ABC123")).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);

        assertThatThrownBy(() -> inscripcionService.crearInscripcion("ABC123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_alumnoYaInscrito_lanzaRuntimeException400() {
        Inscripcion inscripcionExistente = new Inscripcion();
        inscripcionExistente.setId(99L);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("ABC123")).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);
        when(inscripcionRepository.findByAlumnoIdAndCursoId(1L, 10L)).thenReturn(inscripcionExistente);

        assertThatThrownBy(() -> inscripcionService.crearInscripcion("ABC123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("400 Bad Request");

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_usuarioNoEsAlumno_lanzaRuntimeException401() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> inscripcionService.crearInscripcion("ABC123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Solo un alumno puede inscribirse en un curso");

        verify(inscripcionRepository, never()).save(any());
        verify(cursoRepository, never()).existsByCodigo(any());
    }

    @Test
    void crearInscripcion_visibilidadNull_lanzaRuntimeException403() {
        curso.setVisibilidad(null);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("ABC123")).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);

        assertThatThrownBy(() -> inscripcionService.crearInscripcion("ABC123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_inscripcionGuardada_tienePuntosCeroYFechaHoy() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.existsByCodigo("ABC123")).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);
        when(inscripcionRepository.findByAlumnoIdAndCursoId(1L, 10L)).thenReturn(null);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        Inscripcion resultado = inscripcionService.crearInscripcion("ABC123");

        assertThat(resultado.getPuntos()).isEqualTo(0);
        assertThat(resultado.getFechaInscripcion()).isEqualTo(java.time.LocalDate.now());
    }

    @Test
    void listarInscripcionesPorCurso_retornaPuntosRealesDelAlumno() {
        Tema tema = new Tema();
        tema.setId(100L);
        curso.setTemas(List.of(tema));
        curso.setMaestro(maestro);

        Actividad actividad = mock(Actividad.class);

        ActividadAlumno actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(300L);
        actividadAlumno.setAlumno(alumno);
        actividadAlumno.setPuntuacion(42);
        actividadAlumno.setFechaInicio(LocalDateTime.now().minusMinutes(10));
        actividadAlumno.setFechaFin(LocalDateTime.now());
        when(actividad.getActividadesAlumno()).thenReturn(List.of(actividadAlumno));

        Inscripcion inscripcion = new Inscripcion(0, LocalDate.now(), alumno, curso);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(inscripcionRepository.findByCursoIdWithAlumno(10L)).thenReturn(List.of(inscripcion));
        when(actividadRepository.findByTemaId(100L)).thenReturn(List.of(actividad));

        List<AlumnoCursoDTO> resultado = inscripcionService.listarInscripcionesPorCurso(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPuntos()).isEqualTo(42);
        assertThat(resultado.get(0).getAlumnoId()).isEqualTo(1L);
    }
}
