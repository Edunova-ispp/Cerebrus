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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividadalumno.ActividadAlumno;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class EstadisticasMaestroServiceImplTest {

    @Mock
    private EstadisticasMaestroRepository estadisticasRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private EstadisticasMaestroServiceImpl estadisticasService;

    private Maestro maestro;
    private Alumno alumno;
    private Curso curso;

    @BeforeEach
    void setUp() {
        maestro = crearMaestro(1L);
        alumno = crearAlumno(2L, "Alumno 1");
        curso = crearCurso(10L, "Matemáticas", maestro, true);
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_sinInscripciones_retornaMapaVacio() {
        curso.setInscripciones(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(java.util.Optional.of(curso));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);
        assertThat(resultado).isEmpty();
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_actividadesTerminadas_sumaPuntosCorrectamente() {
        Actividad actividad = crearActividad(50);
        ActividadAlumno actividadAlumno = crearActividadAlumnoTerminada(alumno);
        actividad.setActividadesAlumno(List.of(actividadAlumno));

        Tema tema = new Tema();
        tema.setId(1L);

        Inscripcion inscripcion = new Inscripcion(0, LocalDate.now(), alumno, curso);
        curso.setInscripciones(List.of(inscripcion));
        curso.setTemas(List.of(tema));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findByTemaId(1L)).thenReturn(List.of(actividad));
        when(cursoRepository.findById(10L)).thenReturn(java.util.Optional.of(curso));

        HashMap<String, Integer> resultado = estadisticasService.calcularTotalPuntosCursoPorAlumno(10L);
        assertThat(resultado).containsEntry("Alumno 1", 50);
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findById(10L)).thenReturn(java.util.Optional.of(curso));

        assertThatThrownBy(() -> estadisticasService.calcularTotalPuntosCursoPorAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede visualizar los puntos de los alumnos");
    }

    @Test
    void calcularTotalPuntosCursoPorAlumno_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otroMaestro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);
        when(cursoRepository.findById(10L)).thenReturn(java.util.Optional.of(curso));


        assertThatThrownBy(() -> estadisticasService.calcularTotalPuntosCursoPorAlumno(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
    }

    // ---------- helper methods ----------

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

    private static Curso crearCurso(Long id, String titulo, Maestro maestro, boolean visibilidad) {
        Curso c = new Curso();
        c.setId(id);
        c.setTitulo(titulo);
        c.setDescripcion("Descripción de prueba");
        c.setImagen("img.png");
        c.setCodigo("ABC1234");
        c.setVisibilidad(visibilidad);
        c.setMaestro(maestro);
        c.setInscripciones(new ArrayList<>());
        c.setTemas(new ArrayList<>());
        return c;
    }

    private static Actividad crearActividad(int puntuacion) {
        Actividad a = new Actividad() {};
        a.setPuntuacion(puntuacion);
        a.setActividadesAlumno(new ArrayList<>());
        return a;
    }

    private static ActividadAlumno crearActividadAlumnoTerminada(Alumno alumno) {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setAlumno(alumno);
        aa.setTiempo(0);
        aa.setPuntuacion(0);
        aa.setNota(10);
        aa.setInicio(LocalDateTime.now());
        aa.setAcabada(LocalDateTime.now());
        aa.setNumAbandonos(0);
        aa.setRespuestasAlumno(new ArrayList<>(List.of(new com.cerebrus.respuestaalumno.RespuestaAlumno() {{
            setCorrecta(true);
        }})));
        return aa;
    }
}