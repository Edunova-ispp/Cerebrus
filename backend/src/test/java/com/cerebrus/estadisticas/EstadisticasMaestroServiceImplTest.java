package com.cerebrus.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.dto.AlumnosMasRapidosLentosDTO;
import com.cerebrus.estadisticas.dto.ActividadEstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoResumenDTO;
import com.cerebrus.estadisticas.dto.EstadisticasActividadDTO;
import com.cerebrus.estadisticas.dto.EstadisticasCursoDTO;
import com.cerebrus.estadisticas.dto.EstadisticasTemaDTO;
import com.cerebrus.estadisticas.dto.EstadisticasAlumnoDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class EstadisticasMaestroServiceImplTest {

    @Mock private EstadisticasMaestroRepository estadisticasRepository;
    @Mock private CursoRepository cursoRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private ActividadRepository actividadRepository;
    @Mock private TemaRepository temaRepository;

    @InjectMocks
    private EstadisticasMaestroServiceImpl estadisticasService;

    private Maestro maestro;
    private Alumno alumno;
    private Curso curso;

    @BeforeEach
    void setUp() {
        maestro = crearMaestro(1L);
        alumno  = crearAlumno(2L, "Alumno 1");
        curso   = crearCurso(10L, maestro);
    }

    // ==================== calcularTotalPuntosCursoPorAlumno ====================

    @Test
    void calcularTotalPuntosCursoPorAlumno_sinInscripciones_retornaMapaVacio() {
        curso.setInscripciones(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_actividadTerminada_sumaPuntos() {
        Actividad actividad = crearActividad();
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        aa.setPuntuacion(50);
        actividad.setActividadesAlumno(List.of(aa));

        Tema tema = crearTema(1L, curso);
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);

        assertThat(resultado).containsEntry("Alumno 1", 50);
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_variasActividades_acumulaPuntos() {
        Actividad act1 = crearActividad();
        Actividad act2 = crearActividad();
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setPuntuacion(30);
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setPuntuacion(20);
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        Tema tema = crearTema(1L, curso);
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1, act2));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);

        assertThat(resultado).containsEntry("Alumno 1", 50);
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_actividadNoTerminada_noSumaPuntos() {
        Actividad actividad = crearActividad();
        ActividadAlumno aa = crearActividadAlumnoEmpezada(alumno);
        aa.setPuntuacion(50);
        actividad.setActividadesAlumno(List.of(aa));

        Tema tema = crearTema(1L, curso);
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);

        assertThat(resultado).containsEntry("Alumno 1", 0);
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.calcularTotalPuntosCursoPorAlumno(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.calcularTotalPuntosCursoPorAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede visualizar los puntos de los alumnos");
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.calcularTotalPuntosCursoPorAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
    }

    // ==================== numActividadesRealizadasPorAlumno ====================

    @Test
    void numActividadesRealizadasPorAlumno_actividadesTerminadas_cuentaCorrectamente() {
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(estadisticasRepository.findAllByCursoConRespuestas(curso)).thenReturn(List.of(aa1, aa2));

        Map<String, Long> resultado = estadisticasService.numActividadesRealizadasPorAlumno(curso);

        assertThat(resultado).containsEntry("Alumno 1", 2L);
    }

    @Test
    void numActividadesRealizadasPorAlumno_soloTerminadasCuentan_ignoraEnProgreso() {
        ActividadAlumno aaTerminada  = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aaEnProgreso = crearActividadAlumnoEmpezada(alumno);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(estadisticasRepository.findAllByCursoConRespuestas(curso)).thenReturn(List.of(aaTerminada, aaEnProgreso));

        Map<String, Long> resultado = estadisticasService.numActividadesRealizadasPorAlumno(curso);

        assertThat(resultado).containsEntry("Alumno 1", 1L);
    }

    @Test
    void numActividadesRealizadasPorAlumno_sinActividades_retornaMapaVacio() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(estadisticasRepository.findAllByCursoConRespuestas(curso)).thenReturn(List.of());

        Map<String, Long> resultado = estadisticasService.numActividadesRealizadasPorAlumno(curso);

        assertThat(resultado).isEmpty();
    }

    @Test
    void numActividadesRealizadasPorAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.numActividadesRealizadasPorAlumno(curso))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo los maestros");
    }

    @Test
    void numActividadesRealizadasPorAlumno_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);

        assertThatThrownBy(() -> estadisticasService.numActividadesRealizadasPorAlumno(curso))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propietario");
    }

    // ==================== obtenerTiempoAlumnoEnActividad ====================

    @Test
    void obtenerTiempoAlumnoEnActividad_alumnoConRegistro_retornaTiempo() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(45));
        aa.setFechaFin(LocalDateTime.now());
        actividad.setActividadesAlumno(List.of(aa));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnActividad(2L, 5L);

        assertThat(resultado).isEqualTo(45);
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_alumnoSinRegistro_lanzaRuntimeException() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        actividad.setActividadesAlumno(List.of());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnActividad(2L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_actividadNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnActividad(2L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoAlumnoEnActividad_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnActividad(2L, 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerTiempoAlumnoEnTema ====================

    @Test
    void obtenerTiempoAlumnoEnTema_variasActividades_sumaCorrectamente() {
        Tema tema = crearTema(1L, curso);
        Actividad act1 = crearActividad();
        act1.setTema(tema);
        Actividad act2 = crearActividad();
        act2.setTema(tema);
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(20));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        aa2.setFechaFin(LocalDateTime.now());
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1, act2));

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnTema(2L, 1L);

        assertThat(resultado).isEqualTo(50);
    }

    @Test
    void obtenerTiempoAlumnoEnTema_actividadNoTerminada_noSumaTiempo() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoEmpezada(alumno);
        actividad.setActividadesAlumno(List.of(aa));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnTema(2L, 1L);

        assertThat(resultado).isZero();
    }

    @Test
    void obtenerTiempoAlumnoEnTema_sinActividades_retornaCero() {
        Tema tema = crearTema(1L, curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnTema(2L, 1L);

        assertThat(resultado).isZero();
    }

    @Test
    void obtenerTiempoAlumnoEnTema_temaNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnTema(2L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoAlumnoEnTema_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnTema(2L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerTiempoAlumnoEnCurso ====================

    @Test
    void obtenerTiempoAlumnoEnCurso_sinTemas_retornaCero() {
        curso.setTemas(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnCurso(2L, 10L);

        assertThat(resultado).isZero();
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnCurso(2L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnCurso(2L, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoAlumnoEnCurso(2L, 10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propietario");
    }

    // ==================== obtenerTiempoMedioActividad ====================

    @Test
    void obtenerTiempoMedioActividad_dosAlumnos_calculaMediaCorrectamente() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(20));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno2);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(40));
        aa2.setFechaFin(LocalDateTime.now());
        actividad.setActividadesAlumno(List.of(aa1, aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Double resultado = estadisticasService.obtenerTiempoMedioActividad(5L);

        assertThat(resultado).isEqualTo(30.0);
    }

    @Test
    void obtenerTiempoMedioActividad_sinActividadesTerminadas_retornaCero() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        actividad.setActividadesAlumno(List.of());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Double resultado = estadisticasService.obtenerTiempoMedioActividad(5L);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void obtenerTiempoMedioActividad_soloEnProgreso_retornaCero() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoEmpezada(alumno);
        actividad.setActividadesAlumno(List.of(aa));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Double resultado = estadisticasService.obtenerTiempoMedioActividad(5L);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void obtenerTiempoMedioActividad_actividadNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioActividad(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoMedioActividad_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioActividad(5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerTiempoMedioTema ====================

    @Test
    void obtenerTiempoMedioTema_sinActividades_retornaCero() {
        Tema tema = crearTema(1L, curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        Double resultado = estadisticasService.obtenerTiempoMedioTema(1L);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void obtenerTiempoMedioTema_unAlumnoConDosActividades_sumaYCalculaMedia() {
        Tema tema = crearTema(1L, curso);
        Actividad act1 = crearActividad();
        act1.setTema(tema);
        Actividad act2 = crearActividad();
        act2.setTema(tema);
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(20));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(40));
        aa2.setFechaFin(LocalDateTime.now());
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1, act2));

        // alumno con 60 min totales → único valor → media = 60.0
        Double resultado = estadisticasService.obtenerTiempoMedioTema(1L);

        assertThat(resultado).isEqualTo(60.0);
    }

    @Test
    void obtenerTiempoMedioTema_temaNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioTema(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoMedioTema_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioTema(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerTiempoMedioCurso ====================

    @Test
    void obtenerTiempoMedioCurso_sinInscritos_retornaCero() {
        curso.setInscripciones(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        Double resultado = estadisticasService.obtenerTiempoMedioCurso(10L);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void obtenerTiempoMedioCurso_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioCurso(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerTiempoMedioCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioCurso(10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtenerTiempoMedioCurso_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerTiempoMedioCurso(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propietario");
    }

    // ==================== obtenerAlumnosMasRapidosLentosActividad ====================

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_sinAlumnos_retornaListasVacias() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        actividad.setActividadesAlumno(List.of());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosActividad(5L, 3);

        assertThat(resultado.getMasRapidos()).isEmpty();
        assertThat(resultado.getMasLentos()).isEmpty();
        assertThat(resultado.getTiempoPromedio()).isEqualTo(0.0);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_dosAlumnos_ordenaPorTiempoCorrectamente() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(10));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno2);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(50));
        aa2.setFechaFin(LocalDateTime.now());
        actividad.setActividadesAlumno(List.of(aa1, aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosActividad(5L, 1);

        assertThat(resultado.getMasRapidos().get(0).getNombreAlumno()).isEqualTo("Alumno 1");
        assertThat(resultado.getMasLentos().get(0).getNombreAlumno()).isEqualTo("Alumno 2");
        assertThat(resultado.getTiempoPromedio()).isEqualTo(30.0);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_actividadNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosActividad(99L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerAlumnosMasRapidosLentosActividad_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosActividad(5L, 3))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerAlumnosMasRapidosLentosTema ====================

    @Test
    void obtenerAlumnosMasRapidosLentosTema_sinActividades_retornaListasVacias() {
        Tema tema = crearTema(1L, curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosTema(1L, 3);

        assertThat(resultado.getMasRapidos()).isEmpty();
        assertThat(resultado.getMasLentos()).isEmpty();
        assertThat(resultado.getTiempoPromedio()).isEqualTo(0.0);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_dosAlumnos_ordenaPorTiempoTotal() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(15));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno2);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(45));
        aa2.setFechaFin(LocalDateTime.now());
        actividad.setActividadesAlumno(List.of(aa1, aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosTema(1L, 1);

        assertThat(resultado.getMasRapidos().get(0).getNombreAlumno()).isEqualTo("Alumno 1");
        assertThat(resultado.getMasLentos().get(0).getNombreAlumno()).isEqualTo("Alumno 2");
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_temaNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosTema(99L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerAlumnosMasRapidosLentosTema_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosTema(1L, 3))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerAlumnosMasRapidosLentosCurso ====================

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_sinInscritos_retornaListasVacias() {
        curso.setInscripciones(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosCurso(10L, 3);

        assertThat(resultado.getMasRapidos()).isEmpty();
        assertThat(resultado.getMasLentos()).isEmpty();
        assertThat(resultado.getTiempoPromedio()).isEqualTo(0.0);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_dosInscritosConTiempoCero_incluyeAmbos() {
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        curso.setInscripciones(List.of(
                new Inscripcion(0, LocalDate.now(), alumno, curso),
                new Inscripcion(0, LocalDate.now(), alumno2, curso)));
        curso.setTemas(new ArrayList<>());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        AlumnosMasRapidosLentosDTO resultado = estadisticasService.obtenerAlumnosMasRapidosLentosCurso(10L, 3);

        assertThat(resultado.getMasRapidos()).hasSize(2);
        assertThat(resultado.getMasLentos()).hasSize(2);
        assertThat(resultado.getTiempoPromedio()).isEqualTo(0.0);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosCurso(99L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosCurso(10L, 3))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtenerAlumnosMasRapidosLentosCurso_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerAlumnosMasRapidosLentosCurso(10L, 3))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propietario");
    }

    // ==================== temaCompletado ====================

    @Test
    void temaCompletado_todasLasActividadesTerminadas_retornaTrue() {
        Actividad actividad = crearActividad();
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        actividad.setActividadesAlumno(List.of(aa));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        Boolean resultado = estadisticasService.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void temaCompletado_unaActividadNoTerminada_retornaFalse() {
        Actividad act1 = crearActividad();
        Actividad act2 = crearActividad();
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa2 = crearActividadAlumnoEmpezada(alumno);
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1, act2));

        Boolean resultado = estadisticasService.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isFalse();
    }

    @Test
    void temaCompletado_sinActividades_retornaTrue() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        Boolean resultado = estadisticasService.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void temaCompletado_actividadSinRegistroAlumno_retornaFalse() {
        Actividad actividad = crearActividad();
        actividad.setActividadesAlumno(List.of());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        Boolean resultado = estadisticasService.temaCompletado(2L, 10L, 1L);

        assertThat(resultado).isFalse();
    }

    @Test
    void temaCompletado_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.temaCompletado(2L, 99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void temaCompletado_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.temaCompletado(2L, 10L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== notaMediaAlumno ====================

    @Test
    void notaMediaAlumno_dosActividades_calculaMediaCorrectamente() {
        Actividad act1 = crearActividad();
        Actividad act2 = crearActividad();
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setNota(8);
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setNota(6);
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1, act2));

        Integer resultado = estadisticasService.notaMediaAlumno(2L, 10L, 1L);

        assertThat(resultado).isEqualTo(7);
    }

    @Test
    void notaMediaAlumno_sinActividades_retornaCero() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of());

        Integer resultado = estadisticasService.notaMediaAlumno(2L, 10L, 1L);

        assertThat(resultado).isZero();
    }

    @Test
    void notaMediaAlumno_actividadNoTerminada_noContaEnMedia() {
        Actividad actividad = crearActividad();
        ActividadAlumno aa = crearActividadAlumnoEmpezada(alumno);
        aa.setNota(10);
        actividad.setActividadesAlumno(List.of(aa));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        Integer resultado = estadisticasService.notaMediaAlumno(2L, 10L, 1L);

        assertThat(resultado).isZero();
    }

    @Test
    void notaMediaAlumno_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.notaMediaAlumno(2L, 99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void notaMediaAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.notaMediaAlumno(2L, 10L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerResumenEstadisticasAlumno ====================

    @Test
    void obtenerResumenEstadisticasAlumno_alumnoInscritoConActividadCompletada_retornaResumenCompleto() {
        Tema tema = crearTema(1L, curso);
        tema.setTitulo("Tema 1");
        Actividad actividad = crearActividad();
        actividad.setTitulo("Actividad 1");
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        aa.setNota(8);
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(20));
        aa.setFechaFin(LocalDateTime.now());
        aa.setPuntuacion(100);
        aa.setActividad(actividad);
        actividad.setActividadesAlumno(List.of(aa));

        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        EstadisticasAlumnoResumenDTO resultado = estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L);

        assertThat(resultado.getAlumnoId()).isEqualTo(2L);
        assertThat(resultado.getNombreAlumno()).isEqualTo("Alumno 1");
        assertThat(resultado.getNumActividadesCompletadas()).isEqualTo(1);
        assertThat(resultado.getTotalActividades()).isEqualTo(1);
        assertThat(resultado.getTiempoTotalMinutos()).isEqualTo(20);
        assertThat(resultado.getNotaMedia()).isEqualTo(10.0);
        assertThat(resultado.getNotaMax()).isEqualTo(10.0);
        assertThat(resultado.getNotaMin()).isEqualTo(10.0);
    }

    @Test
    void obtenerResumenEstadisticasAlumno_variasRepeticiones_retornaHistorialCompleto() {
        Tema tema = crearTema(1L, curso);
        tema.setTitulo("Tema 1");
        Actividad actividad = crearActividad();
        actividad.setTitulo("Actividad 1");

        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setId(11L);
        aa1.setNota(6);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(40));
        aa1.setFechaFin(LocalDateTime.now().minusMinutes(30));
        aa1.setPuntuacion(60);

        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setId(12L);
        aa2.setNota(9);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(20));
        aa2.setFechaFin(LocalDateTime.now().minusMinutes(10));
        aa2.setPuntuacion(90);

        actividad.setActividadesAlumno(List.of(aa1, aa2));
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        EstadisticasAlumnoResumenDTO resultado = estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L);

        List<ActividadEstadisticasAlumnoDTO> actividades = resultado.getTemas().get(0).getActividades();
        assertThat(actividades).hasSize(1);
        assertThat(actividades.get(0).getNotaAlumno()).isEqualTo(9);
        assertThat(actividades.get(0).getPuntuacionAlumno()).isEqualTo(90);
        assertThat(actividades.get(0).getIntentos()).hasSize(2);
        assertThat(actividades.get(0).getIntentos().get(0).getId()).isEqualTo(11L);
        assertThat(actividades.get(0).getIntentos().get(1).getId()).isEqualTo(12L);
    }

    @Test
    void obtenerResumenEstadisticasAlumno_conIntentoEnCurso_yTerminada_retornaAmbosIntentos() {
        Tema tema = crearTema(1L, curso);
        tema.setTitulo("Tema 1");
        Actividad actividad = crearActividad();
        actividad.setTitulo("Actividad 1");

        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setId(11L);
        aa1.setNota(7);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(50));
        aa1.setFechaFin(LocalDateTime.now().minusMinutes(40));
        aa1.setPuntuacion(70);

        ActividadAlumno aa2 = new ActividadAlumno();
        aa2.setId(12L);
        aa2.setAlumno(alumno);
        aa2.setActividad(actividad);
        aa2.setNota(0);
        aa2.setPuntuacion(0);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(10));
        aa2.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0));
        aa2.setNumAbandonos(0);

        actividad.setActividadesAlumno(List.of(aa1, aa2));
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        EstadisticasAlumnoResumenDTO resultado = estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L);

        List<ActividadEstadisticasAlumnoDTO> actividades = resultado.getTemas().get(0).getActividades();
        assertThat(actividades.get(0).getNotaAlumno()).isEqualTo(7);
        assertThat(actividades.get(0).getIntentos()).hasSize(2);
        assertThat(actividades.get(0).getIntentos().get(0).getId()).isEqualTo(11L);
        assertThat(actividades.get(0).getIntentos().get(1).getId()).isEqualTo(12L);
    }

    @Test
    void obtenerResumenEstadisticasAlumno_actividadNoCompletada_actividadesCompletadasEsCero() {
        Tema tema = crearTema(1L, curso);
        tema.setTitulo("Tema 1");
        Actividad actividad = crearActividad();
        actividad.setTitulo("Actividad 1");
        actividad.setActividadesAlumno(List.of());

        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        EstadisticasAlumnoResumenDTO resultado = estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L);

        assertThat(resultado.getNumActividadesCompletadas()).isZero();
        assertThat(resultado.getNotaMedia()).isEqualTo(0.0);
        assertThat(resultado.getNotaMin()).isNull();
        assertThat(resultado.getNotaMax()).isNull();
    }

    @Test
    void obtenerResumenEstadisticasAlumno_alumnoNoInscrito_lanzaRuntimeException() {
        curso.setInscripciones(new ArrayList<>());
        curso.setTemas(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerResumenEstadisticasAlumno(10L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerResumenEstadisticasAlumno_cursoNoExiste_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticasService.obtenerResumenEstadisticasAlumno(99L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void obtenerResumenEstadisticasAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtenerResumenEstadisticasAlumno_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerResumenEstadisticasAlumno(10L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propietario");
    }

    // ==================== Helpers ====================

    private static Maestro crearMaestro(Long id) {
        Maestro m = new Maestro();
        m.setId(id);
        return m;
    }

    private static Alumno crearAlumno(Long id, String nombre) {
        Alumno a = new Alumno();
        a.setId(id);
        a.setNombre(nombre);
        a.setPuntos(0);
        return a;
    }

    private static Curso crearCurso(Long id, Maestro maestro) {
        Curso c = new Curso();
        c.setId(id);
        c.setTitulo("Curso Test");
        c.setDescripcion("Descripción");
        c.setImagen("img.png");
        c.setCodigo("TEST01");
        c.setVisibilidad(true);
        c.setMaestro(maestro);
        c.setInscripciones(new ArrayList<>());
        c.setTemas(new ArrayList<>());
        return c;
    }

    private static Tema crearTema(Long id, Curso curso) {
        Tema t = new Tema();
        t.setId(id);
        t.setCurso(curso);
        t.setActividades(new ArrayList<>());
        return t;
    }

    private static Actividad crearActividad() {
        Actividad a = new Actividad() {};
        a.setPuntuacion(100);
        a.setActividadesAlumno(new ArrayList<>());
        return a;
    }

    private static ActividadAlumno crearActividadAlumnoTerminada(Alumno alumno) {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setAlumno(alumno);
        aa.setPuntuacion(0);
        aa.setNota(10);
        aa.setNumAbandonos(0);
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        aa.setFechaFin(LocalDateTime.now());
        aa.setRespuestasAlumno(new ArrayList<>());
        return aa;
    }

    private static ActividadAlumno crearActividadAlumnoEmpezada(Alumno alumno) {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setAlumno(alumno);
        aa.setPuntuacion(0);
        aa.setNota(10);
        aa.setNumAbandonos(0);
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        aa.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0)); // epoch, so not finished
        aa.setRespuestasAlumno(new ArrayList<>());
        return aa;
    }

    // ==================== obtenerEstadisticasCursoActividad ====================

    @Test
    void obtenerEstadisticasCursoActividad_conActividadesTerminadas_retornaEstadisticas() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setId(5L);
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        aa.setNota(8);
        actividad.setActividadesAlumno(List.of(aa));

        curso.setTemas(List.of(tema));
        tema.setActividades(List.of(actividad));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        Map<Long, EstadisticasActividadDTO> resultado = 
            estadisticasService.obtenerEstadisticasCursoActividad(10L, 1L);

        assertThat(resultado).containsKey(5L);
    }

    @Test
    void obtenerEstadisticasCursoActividad_usuarioNoMaestro_lanzaAccessDeniedException() {
        Tema tema = crearTema(1L, curso);
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerEstadisticasCursoActividad(10L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtenerEstadisticasCursoActividad_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otro = crearMaestro(99L);
        Tema tema = crearTema(1L, curso);
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(otro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.obtenerEstadisticasCursoActividad(10L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerRepeticionesCursoActividad ====================

    @Test
    void obtenerRepeticionesCursoActividad_conRepeticiones_retornaDTO() {
        Tema tema = crearTema(1L, curso);
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        Actividad actividad = crearActividad();
        actividad.setId(5L);
        actividad.setTema(tema);
        
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa3 = crearActividadAlumnoTerminada(alumno2);
        actividad.setActividadesAlumno(List.of(aa1, aa2, aa3));

        curso.setTemas(List.of(tema));
        tema.setActividades(List.of(actividad));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        Map<Long, RepeticionesActividadDTO> resultado = 
            estadisticasService.obtenerRepeticionesCursoActividad(10L, 1L);

        assertThat(resultado).containsKey(5L);
        assertThat(resultado.get(5L).getRepeticionesMedia()).isGreaterThan(0);
    }

    // ==================== obtenerEstadisticasCursoTema ====================

    @Test
    void obtenerEstadisticasCursoTema_conTemas_retornaEstadisticas() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        actividad.setActividadesAlumno(List.of(aa));

        curso.setTemas(List.of(tema));
        tema.setActividades(List.of(actividad));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        Map<Long, EstadisticasTemaDTO> resultado = 
            estadisticasService.obtenerEstadisticasCursoTema(10L);

        assertThat(resultado).containsKey(1L);
    }

    // ==================== obtenerEstadisticasCurso ====================

    @Test
    void obtenerEstadisticasCurso_conCursoCompleto_retornaEstadisticas() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        actividad.setActividadesAlumno(List.of(aa));

        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema));
        tema.setActividades(List.of(actividad));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        EstadisticasCursoDTO resultado = 
            estadisticasService.obtenerEstadisticasCurso(10L);

        assertThat(resultado).isNotNull();
    }

    // ==================== obtenerEstadisticasAlumno ====================

    @Test
    void obtenerEstadisticasAlumno_alumnoConActividadCompletada_retornaEstadisticas() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setId(5L);
        actividad.setTema(tema);
        actividad.setTitulo("Actividad 1");
        ActividadAlumno aa = crearActividadAlumnoTerminada(alumno);
        aa.setActividad(actividad);
        actividad.setActividadesAlumno(List.of(aa));

        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));

        Map<Long, EstadisticasAlumnoDTO> resultado = 
            estadisticasService.obtenerEstadisticasAlumno(2L, 10L, 1L);

        assertThat(resultado).containsKey(5L);
    }

    @Test
    void obtenerEstadisticasAlumno_suarioNoMaestro_lanzaAccessDeniedException() {
        Tema tema = crearTema(1L, curso);
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> estadisticasService.obtenerEstadisticasAlumno(2L, 10L, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== obtenerRepeticionesActividad ====================

    @Test
    void obtenerRepeticionesActividad_conRepeticiones_retornaDTO() {
        Tema tema = crearTema(1L, curso);
        Alumno alumno2 = crearAlumno(3L, "Alumno 2");
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        ActividadAlumno aa3 = crearActividadAlumnoTerminada(alumno2);
        actividad.setActividadesAlumno(List.of(aa1, aa2, aa3));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        com.cerebrus.estadisticas.dto.RepeticionesActividadDTO resultado = 
            estadisticasService.obtenerRepeticionesActividad(5L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRepeticionesMedia()).isGreaterThan(0);
    }

    @Test
    void obtenerRepeticionesActividad_sinRepeticiones_retornaValoresPorDefecto() {
        Tema tema = crearTema(1L, curso);
        Actividad actividad = crearActividad();
        actividad.setTema(tema);
        actividad.setActividadesAlumno(List.of());

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        com.cerebrus.estadisticas.dto.RepeticionesActividadDTO resultado = 
            estadisticasService.obtenerRepeticionesActividad(5L);

        assertThat(resultado.getRepeticionesMedia()).isEqualTo(0.0);
        assertThat(resultado.getRepeticionesMinima()).isEqualTo(0);
        assertThat(resultado.getRepeticionesMaxima()).isEqualTo(0);
    }

    @Test
    void obtenerTiempoAlumnoEnCurso_variosTemasConTiempo_sumaCorrectamente() {
        Tema tema1 = crearTema(1L, curso);
        Tema tema2 = crearTema(2L, curso);
        Actividad act1 = crearActividad();
        act1.setTema(tema1);
        Actividad act2 = crearActividad();
        act2.setTema(tema2);
        
        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setFechaInicio(LocalDateTime.now().minusMinutes(25));
        aa1.setFechaFin(LocalDateTime.now());
        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setFechaInicio(LocalDateTime.now().minusMinutes(35));
        aa2.setFechaFin(LocalDateTime.now());
        
        act1.setActividadesAlumno(List.of(aa1));
        act2.setActividadesAlumno(List.of(aa2));

        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));
        curso.setTemas(List.of(tema1, tema2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema1));
        when(temaRepository.findById(2L)).thenReturn(Optional.of(tema2));
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(act1));
        when(actividadRepository.findByTemaId(2L)).thenReturn(List.of(act2));

        Integer resultado = estadisticasService.obtenerTiempoAlumnoEnCurso(2L, 10L);

        assertThat(resultado).isGreaterThan(0);
    }

}