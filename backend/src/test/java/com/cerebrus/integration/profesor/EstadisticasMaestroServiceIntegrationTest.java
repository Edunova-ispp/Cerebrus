package com.cerebrus.integration.profesor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cerebrus.actividad.ActividadRepository;
import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.marcarImagen.MarcarImagen;
import com.cerebrus.actividad.ordenacion.Ordenacion;
import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.comun.enumerados.TipoActGeneral;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.estadisticas.EstadisticasMaestroRepository;
import com.cerebrus.estadisticas.EstadisticasMaestroServiceImpl;
import com.cerebrus.estadisticas.dto.IntentoActividadDTO;
import com.cerebrus.estadisticas.dto.IntentoActividadDetalleDTO;
import com.cerebrus.estadisticas.dto.IntentoDetalleRespuestaDTO;
import com.cerebrus.estadisticas.dto.RepeticionesActividadDTO;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacion;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaMaestro.RespuestaMaestro;
import com.cerebrus.respuestaMaestro.RespuestaMaestroRepository;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@SpringBootTest(properties = {
    "GOOGLE_API_KEY_1=dummy-key-1",
    "GOOGLE_API_KEY_2=dummy-key-2",
    "GOOGLE_API_KEY_3=dummy-key-3",
    "GOOGLE_API_KEY_4=dummy-key-4",
    "GOOGLE_API_KEY_5=dummy-key-5"
})
class EstadisticasMaestroServiceIntegrationTest {

    @MockitoBean
    private EstadisticasMaestroRepository estadisticasRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private ActividadRepository actividadRepository;

    @MockitoBean
    private CursoRepository cursoRepository;

    @MockitoBean
    private TemaRepository temaRepository;

    @MockitoBean
    private RespuestaMaestroRepository respuestaMaestroRepository;

    @Autowired
    private EstadisticasMaestroServiceImpl service;

    private Maestro maestro;
    private Alumno alumno;
    private Curso curso;
    private Tema tema;

    @BeforeEach
    void setUp() {
        maestro = crearMaestro(1L, "Maestro 1");
        alumno = crearAlumno(2L, "Alumno 1");
        curso = crearCurso(10L, maestro);
        tema = crearTema(20L, curso);
        curso.setTemas(List.of(tema));
    }

    @Test
    void obtenerDetalleIntento_generalClasificacion_mapeaRespuestasYTipoActividad() {
        General actividad = crearGeneral(30L, tema, TipoActGeneral.CLASIFICACION, "general.png");
        Pregunta pregunta = new Pregunta();
        pregunta.setId(100L);
        pregunta.setPregunta("Pregunta general");
        pregunta.setActividad(actividad);

        ActividadAlumno intento = crearIntento(40L, alumno, actividad, 15, 45, 90, 8, 0);

        RespAlumnoGeneral respuestaGeneral = new RespAlumnoGeneral(false, intento, "respuesta-correcta", pregunta);
        respuestaGeneral.setId(401L);
        respuestaGeneral.setNumFallos(2);

        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setRespuesta("punto correcto");
        RespAlumnoPuntoImagen respuestaPuntoImagen = new RespAlumnoPuntoImagen(true, intento, "respuesta punto", puntoImagen);
        respuestaPuntoImagen.setId(402L);

        Ordenacion auxiliarOrdenacion = new Ordenacion();
        RespAlumnoOrdenacion respuestaOrdenacion = new RespAlumnoOrdenacion(true, intento, List.of("A", "B"), auxiliarOrdenacion);
        respuestaOrdenacion.setId(403L);

        intento.setRespuestasAlumno(List.of(respuestaGeneral, respuestaPuntoImagen, respuestaOrdenacion));
        actividad.setActividadesAlumno(List.of(intento));

        RespuestaMaestro respuestaMaestro = new RespuestaMaestro();
        respuestaMaestro.setId(501L);
        respuestaMaestro.setRespuesta("respuesta-correcta");
        respuestaMaestro.setPregunta(pregunta);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(30L)).thenReturn(Optional.of(actividad));
        when(respuestaMaestroRepository.findByRespuesta("respuesta-correcta")).thenReturn(Optional.of(respuestaMaestro));

        IntentoActividadDetalleDTO resultado = service.obtenerDetalleIntento(10L, 2L, 30L, 40L);

        assertThat(resultado.getIntentoId()).isEqualTo(40L);
        assertThat(resultado.getActividadTipo()).isEqualTo("Clasificacion");
        assertThat(resultado.getActividadImagen()).isEqualTo("general.png");
        assertThat(resultado.getRespuestas()).hasSize(3);
        assertThat(resultado.getRespuestas().get(0))
                .extracting(IntentoDetalleRespuestaDTO::getTipoRespuesta, IntentoDetalleRespuestaDTO::getEnunciado,
                        IntentoDetalleRespuestaDTO::getRespuestaAlumno, IntentoDetalleRespuestaDTO::getCorrecta,
                        IntentoDetalleRespuestaDTO::getNumFallos)
                .containsExactly("GENERAL", "Pregunta general", "respuesta-correcta", true, 2);
        assertThat(resultado.getRespuestas().get(1).getTipoRespuesta()).isEqualTo("PUNTO_IMAGEN");
        assertThat(resultado.getRespuestas().get(1).getEnunciado()).isEqualTo("Punto imagen: punto correcto");
        assertThat(resultado.getRespuestas().get(2).getTipoRespuesta()).isEqualTo("ORDENACION");
        assertThat(resultado.getRespuestas().get(2).getRespuestaAlumno()).isEqualTo("A > B");
    }

    @Test
    void obtenerDetalleIntento_marcarImagen_retornaImagenEspecifica() {
        MarcarImagen actividad = crearMarcarImagen(31L, tema, "imagen-a-marcar.png", "fallback.png");
        ActividadAlumno intento = crearIntento(41L, alumno, actividad, 20, 0, 40, 7, 1);
        intento.setRespuestasAlumno(List.of());
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(31L)).thenReturn(Optional.of(actividad));

        IntentoActividadDetalleDTO resultado = service.obtenerDetalleIntento(10L, 2L, 31L, 41L);

        assertThat(resultado.getActividadTipo()).isEqualTo("MarcarImagen");
        assertThat(resultado.getActividadImagen()).isEqualTo("imagen-a-marcar.png");
        assertThat(resultado.getRespuestas()).isEmpty();
    }

    @Test
    void obtenerDetalleIntento_ordenacion_retornaTipoOrdenacion() {
        Ordenacion actividad = crearOrdenacion(32L, tema, "ordenacion.png");
        ActividadAlumno intento = crearIntento(42L, alumno, actividad, 25, 10, 55, 6, 0);
        RespAlumnoOrdenacion respuestaOrdenacion = new RespAlumnoOrdenacion(true, intento, List.of("uno", "dos"), actividad);
        respuestaOrdenacion.setId(404L);
        intento.setRespuestasAlumno(List.of(respuestaOrdenacion));
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(32L)).thenReturn(Optional.of(actividad));

        IntentoActividadDetalleDTO resultado = service.obtenerDetalleIntento(10L, 2L, 32L, 42L);

        assertThat(resultado.getActividadTipo()).isEqualTo("Ordenacion");
        assertThat(resultado.getActividadImagen()).isEqualTo("ordenacion.png");
        assertThat(resultado.getRespuestas()).hasSize(1);
        assertThat(resultado.getRespuestas().get(0).getTipoRespuesta()).isEqualTo("ORDENACION");
    }

    @Test
    void obtenerDetalleIntento_intentoNoPerteneceAlAlumno_lanzaRuntimeException() {
        General actividad = crearGeneral(33L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno intento = crearIntento(43L, alumno, actividad, 10, 0, 30, 4, 0);
        intento.setRespuestasAlumno(List.of());
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(33L)).thenReturn(Optional.of(actividad));

        assertThatThrownBy(() -> service.obtenerDetalleIntento(10L, 999L, 33L, 43L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El intento no pertenece al alumno indicado");
    }

    @Test
    void actualizarPuntuacionIntento_actualizaYCalculaNota() {
        General actividad = crearGeneral(34L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno intento = crearIntento(44L, alumno, actividad, 12, 30, 60, 5, 0);
        intento.setPuntuacion(20);
        intento.setNota(2);
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(34L)).thenReturn(Optional.of(actividad));

        IntentoActividadDTO resultado = service.actualizarPuntuacionIntento(10L, 2L, 34L, 44L, 50);

        assertThat(resultado.getPuntuacion()).isEqualTo(50);
        assertThat(resultado.getNota()).isEqualTo(5);
        assertThat(intento.getPuntuacion()).isEqualTo(50);
        assertThat(intento.getNota()).isEqualTo(5);
    }

    @Test
    void actualizarPuntuacionIntento_nuevaPuntuacionNegativa_seNormalizaAZero() {
        General actividad = crearGeneral(35L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno intento = crearIntento(45L, alumno, actividad, 12, 30, 60, 5, 0);
        intento.setPuntuacion(20);
        intento.setNota(2);
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(35L)).thenReturn(Optional.of(actividad));

        IntentoActividadDTO resultado = service.actualizarPuntuacionIntento(10L, 2L, 35L, 45L, -7);

        assertThat(resultado.getPuntuacion()).isZero();
        assertThat(resultado.getNota()).isZero();
    }

    @Test
    void actualizarPuntuacionIntento_puntuacionNula_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> service.actualizarPuntuacionIntento(10L, 2L, 34L, 44L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La puntuación es obligatoria.");
    }

    @Test
    void actualizarPuntuacionIntento_puntuacionMayorALaMaxima_lanzaIllegalArgumentException() {
        General actividad = crearGeneral(36L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno intento = crearIntento(46L, alumno, actividad, 12, 30, 60, 5, 0);
        actividad.setActividadesAlumno(List.of(intento));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));
        when(actividadRepository.findById(36L)).thenReturn(Optional.of(actividad));

        assertThatThrownBy(() -> service.actualizarPuntuacionIntento(10L, 2L, 36L, 46L, 120))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede superar");
    }

    @Test
    void obtenerTiempoAlumnoEnTema_sinActividades_retornaCero() {
        Tema temaVacio = crearTema(21L, curso);

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(temaRepository.findById(21L)).thenReturn(Optional.of(temaVacio));
        when(actividadRepository.findByTemaId(21L)).thenReturn(List.of());

        Integer resultado = service.obtenerTiempoAlumnoEnTema(2L, 21L);

        assertThat(resultado).isZero();
    }

    @Test
    void numActividadesRealizadasPorAlumno_terminadasCuentaCorrectamente() {
        General actividad = crearGeneral(37L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno actividadTerminada = crearIntento(47L, alumno, actividad, 5, 0, 10, 1, 0);
        curso.setInscripciones(List.of(new Inscripcion(0, LocalDate.now(), alumno, curso)));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(estadisticasRepository.findAllByCursoConRespuestas(curso)).thenReturn(List.of(actividadTerminada));

        Map<String, Long> resultado = service.numActividadesRealizadasPorAlumno(curso);

        assertThat(resultado).containsEntry("Alumno 1", 1L);
    }

    @Test
    void obtenerRepeticionesActividad_conRepeticiones_retornaDTO() {
        General actividad = crearGeneral(38L, tema, TipoActGeneral.TEORIA, "general.png");
        ActividadAlumno intento1 = crearIntento(48L, alumno, actividad, 5, 0, 10, 1, 0);
        ActividadAlumno intento2 = crearIntento(49L, alumno, actividad, 6, 0, 10, 1, 0);
        actividad.setActividadesAlumno(List.of(intento1, intento2));

        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadRepository.findById(38L)).thenReturn(Optional.of(actividad));

        RepeticionesActividadDTO resultado = service.obtenerRepeticionesActividad(38L);

        assertThat(resultado.getRepeticionesMedia()).isEqualTo(2.0);
        assertThat(resultado.getRepeticionesMinima()).isEqualTo(2);
        assertThat(resultado.getRepeticionesMaxima()).isEqualTo(2);
    }

    private static Maestro crearMaestro(Long id, String nombre) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        maestro.setNombre(nombre);
        return maestro;
    }

    private static Alumno crearAlumno(Long id, String nombre) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        alumno.setNombre(nombre);
        alumno.setPuntos(0);
        return alumno;
    }

    private static Curso crearCurso(Long id, Maestro maestro) {
        Curso curso = new Curso();
        curso.setId(id);
        curso.setTitulo("Curso test");
        curso.setDescripcion("Descripcion");
        curso.setCodigo("TEST01");
        curso.setVisibilidad(true);
        curso.setMaestro(maestro);
        curso.setInscripciones(new ArrayList<>());
        curso.setTemas(new ArrayList<>());
        return curso;
    }

    private static Tema crearTema(Long id, Curso curso) {
        Tema tema = new Tema();
        tema.setId(id);
        tema.setTitulo("Tema test");
        tema.setCurso(curso);
        tema.setActividades(new ArrayList<>());
        return tema;
    }

    private static General crearGeneral(Long id, Tema tema, TipoActGeneral tipo, String imagen) {
        General actividad = new General();
        actividad.setId(id);
        actividad.setTitulo("General test");
        actividad.setDescripcion("Descripcion");
        actividad.setPuntuacion(100);
        actividad.setImagen(imagen);
        actividad.setRespVisible(true);
        actividad.setPosicion(1);
        actividad.setVersion(1);
        actividad.setTema(tema);
        actividad.setTipo(tipo);
        actividad.setActividadesAlumno(new ArrayList<>());
        return actividad;
    }

    private static MarcarImagen crearMarcarImagen(Long id, Tema tema, String imagenAMarcar, String imagen) {
        MarcarImagen actividad = new MarcarImagen();
        actividad.setId(id);
        actividad.setTitulo("Marcar imagen test");
        actividad.setDescripcion("Descripcion");
        actividad.setPuntuacion(100);
        actividad.setImagen(imagen);
        actividad.setRespVisible(true);
        actividad.setPosicion(1);
        actividad.setVersion(1);
        actividad.setTema(tema);
        actividad.setImagenAMarcar(imagenAMarcar);
        actividad.setActividadesAlumno(new ArrayList<>());
        return actividad;
    }

    private static Ordenacion crearOrdenacion(Long id, Tema tema, String imagen) {
        Ordenacion actividad = new Ordenacion();
        actividad.setId(id);
        actividad.setTitulo("Ordenacion test");
        actividad.setDescripcion("Descripcion");
        actividad.setPuntuacion(100);
        actividad.setImagen(imagen);
        actividad.setRespVisible(true);
        actividad.setPosicion(1);
        actividad.setVersion(1);
        actividad.setTema(tema);
        actividad.setValores(List.of("uno", "dos", "tres"));
        actividad.setActividadesAlumno(new ArrayList<>());
        return actividad;
    }

    private static ActividadAlumno crearIntento(Long id, Alumno alumno, com.cerebrus.actividad.Actividad actividad,
            int tiempoMinutos, int tiempoSegundos, int puntuacion, int nota, int numAbandonos) {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusMinutes(tiempoMinutos).minusSeconds(tiempoSegundos);
        ActividadAlumno intento = new ActividadAlumno(puntuacion, fechaInicio, fechaFin, nota, numAbandonos, alumno, actividad);
        intento.setId(id);
        intento.setRespuestasAlumno(new ArrayList<>());
        return intento;
    }
}
