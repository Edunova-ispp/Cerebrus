package com.cerebrus.inscripcionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.inscripcion.InscripcionRepository;
import com.cerebrus.inscripcion.InscripcionServiceImpl;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.suscripcion.SuscripcionRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.organizacion.Organizacion;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    private Alumno alumno;
    private Organizacion organizacion;
    private Curso curso;

    @BeforeEach
    void setUp() {
        organizacion = new Organizacion();
        organizacion.setId(7L);

        alumno = new Alumno();
        alumno.setId(10L);
        alumno.setOrganizacion(organizacion);

        curso = new Curso();
        curso.setId(5L);
        curso.setCodigo("ABC123");
        curso.setVisibilidad(true);
    }

    @Test
    void crearInscripcion_suscripcionActivaYCupoDisponible_creaInscripcion() {
        Suscripcion suscripcion = crearSuscripcionActiva(30);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(suscripcionRepository
                .findTopByOrganizacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        7L, LocalDate.now(), LocalDate.now()))
                .thenReturn(Optional.of(suscripcion));
        when(inscripcionRepository.existsByAlumnoId(10L)).thenReturn(false);
        when(inscripcionRepository.countDistinctAlumnosInscritosByOrganizacionId(7L)).thenReturn(12L);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);
        when(inscripcionRepository.findByAlumnoIdAndCursoId(10L, 5L)).thenReturn(null);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion creada = inscripcionService.CrearInscripcion(" abc123 ");

        assertThat(creada).isNotNull();
        assertThat(creada.getAlumno()).isEqualTo(alumno);
        assertThat(creada.getCurso()).isEqualTo(curso);
        assertThat(creada.getPuntos()).isZero();
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_sinSuscripcionActiva_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(suscripcionRepository
                .findTopByOrganizacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        7L, LocalDate.now(), LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.CrearInscripcion("ABC123"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("La organizacion no tiene una suscripcion activa");
    }

    @Test
    void crearInscripcion_cupoDeAlumnosSuperado_lanzaAccessDenied() {
        Suscripcion suscripcion = crearSuscripcionActiva(10);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(suscripcionRepository
                .findTopByOrganizacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        7L, LocalDate.now(), LocalDate.now()))
                .thenReturn(Optional.of(suscripcion));
        when(inscripcionRepository.existsByAlumnoId(10L)).thenReturn(false);
        when(inscripcionRepository.countDistinctAlumnosInscritosByOrganizacionId(7L)).thenReturn(10L);

        assertThatThrownBy(() -> inscripcionService.CrearInscripcion("ABC123"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Se ha alcanzado el cupo de alumnos de la suscripcion activa");
    }

    @Test
    void crearInscripcion_alumnoYaInscritoEnOtroCurso_noConsumeCupoYPermiteNuevaInscripcion() {
        Suscripcion suscripcion = crearSuscripcionActiva(1);

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(suscripcionRepository
                .findTopByOrganizacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        7L, LocalDate.now(), LocalDate.now()))
                .thenReturn(Optional.of(suscripcion));
        when(inscripcionRepository.existsByAlumnoId(10L)).thenReturn(true);
        when(cursoRepository.findByCodigo("ABC123")).thenReturn(curso);
        when(inscripcionRepository.findByAlumnoIdAndCursoId(10L, 5L)).thenReturn(null);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion creada = inscripcionService.CrearInscripcion("ABC123");

        assertThat(creada).isNotNull();
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    private Suscripcion crearSuscripcionActiva(int cupoAlumnos) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setNumAlumnos(cupoAlumnos);
        suscripcion.setFechaInicio(LocalDate.now().minusDays(3));
        suscripcion.setFechaFin(LocalDate.now().plusDays(30));
        return suscripcion;
    }
}
