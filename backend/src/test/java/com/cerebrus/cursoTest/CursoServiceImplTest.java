package com.cerebrus.cursoTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.cerebrus.actividadalumno.ActividadAlumnoProgreso;
import com.cerebrus.actividadalumno.ActividadAlumnoRepository;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.curso.ProgresoDTO;
import com.cerebrus.inscripcion.Inscripcion;
import com.cerebrus.respuestaalumno.RespuestaAlumno;
import com.cerebrus.tema.Tema;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ActividadAlumnoRepository actividadAlumnoRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private CursoServiceImpl cursoService;

    private Maestro maestro;
    private Alumno alumno;
    private Usuario usuarioGenerico;
    private Curso curso;

    @BeforeEach
    void setUp() {
        maestro = crearMaestro(1L);
        alumno = crearAlumno(2L);

        // Usuario genérico que no es ni Maestro ni Alumno
        usuarioGenerico = new Usuario() {};

        curso = crearCurso(10L, "Matemáticas", maestro, true);
    }

    // -------------------------------------------------------
    // ObtenerCursosUsuarioLogueado
    // -------------------------------------------------------

    // Test para verificar que ObtenerCursosUsuarioLogueado retorna los cursos del maestro logueado
    @Test
    void obtenerCursosUsuarioLogueado_maestro_retornaCursosPropios() {
        List<Curso> cursosMaestro = List.of(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findByMaestroId(1L)).thenReturn(cursosMaestro);

        List<Curso> resultado = cursoService.ObtenerCursosUsuarioLogueado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(curso);
        verify(cursoRepository).findByMaestroId(1L);
    }

    // Test para verificar que ObtenerCursosUsuarioLogueado retorna los cursos visibles del alumno logueado
    @Test
    void obtenerCursosUsuarioLogueado_alumno_retornaCursosInscritos() {
        List<Curso> cursosAlumno = List.of(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(cursosAlumno);

        List<Curso> resultado = cursoService.ObtenerCursosUsuarioLogueado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(curso);
        verify(cursoRepository).findByAlumnoId(2L);
    }

    // Test para verificar que ObtenerCursosUsuarioLogueado lanza RuntimeException cuando el usuario no es ni Maestro ni Alumno
    @Test
    void obtenerCursosUsuarioLogueado_usuarioGenerico_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioGenerico);

        assertThatThrownBy(() -> cursoService.ObtenerCursosUsuarioLogueado())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // -------------------------------------------------------
    // obtenerDetallesCurso
    // -------------------------------------------------------

    // Test para verificar que obtenerDetallesCurso retorna titulo, descripcion, imagen y codigo para el maestro propietario
    @Test
    void obtenerDetallesCurso_maestroPropietario_retornaDetallesConCodigo() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        List<String> resultado = cursoService.obtenerDetallesCurso(10L);

        assertThat(resultado).containsExactly("Matemáticas", curso.getDescripcion(), curso.getImagen(), curso.getCodigo());
    }

    // Test para verificar que obtenerDetallesCurso lanza RuntimeException 403 cuando el maestro no es propietario del curso
    @Test
    void obtenerDetallesCurso_maestroNoPropietario_lanzaForbidden() {
        Maestro otroMaestro = crearMaestro(99L);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.obtenerDetallesCurso(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que obtenerDetallesCurso retorna titulo, descripcion e imagen para alumno inscrito en curso visible
    @Test
    void obtenerDetallesCurso_alumnoInscritoYVisible_retornaDetallesSinCodigo() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of(curso));

        List<String> resultado = cursoService.obtenerDetallesCurso(10L);

        assertThat(resultado).containsExactly("Matemáticas", curso.getDescripcion(), curso.getImagen());
        assertThat(resultado).doesNotContain(curso.getCodigo());
    }

    // Test para verificar que obtenerDetallesCurso lanza RuntimeException 403 cuando el alumno está inscrito pero el curso no es visible
    @Test
    void obtenerDetallesCurso_alumnoInscritoYNoVisible_lanzaForbidden() {
        curso.setVisibilidad(false);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of(curso));

        assertThatThrownBy(() -> cursoService.obtenerDetallesCurso(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que obtenerDetallesCurso lanza RuntimeException 403 cuando el alumno no está inscrito en el curso
    @Test
    void obtenerDetallesCurso_alumnoNoInscrito_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of());

        assertThatThrownBy(() -> cursoService.obtenerDetallesCurso(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que obtenerDetallesCurso lanza RuntimeException 404 cuando el curso no existe
    @Test
    void obtenerDetallesCurso_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.obtenerDetallesCurso(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");
    }

    // Test para verificar que obtenerDetallesCurso lanza RuntimeException 403 cuando el usuario no es ni Maestro ni Alumno
    @Test
    void obtenerDetallesCurso_usuarioGenerico_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(usuarioGenerico);

        assertThatThrownBy(() -> cursoService.obtenerDetallesCurso(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // -------------------------------------------------------
    // cambiarVisibilidad
    // -------------------------------------------------------

    // Test para verificar que cambiarVisibilidad cambia de true a false correctamente cuando el maestro es propietario
    @Test
    void cambiarVisibilidad_maestroPropietario_cambiaDeVisibleAOculto() {
        curso.setVisibilidad(true);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.save(curso)).thenReturn(curso);

        Curso resultado = cursoService.cambiarVisibilidad(10L);

        assertThat(resultado.getVisibilidad()).isFalse();
        verify(cursoRepository).save(curso);
    }

    // Test para verificar que cambiarVisibilidad cambia de false a true correctamente cuando el maestro es propietario
    @Test
    void cambiarVisibilidad_maestroPropietario_cambiaDeOcultoAVisible() {
        curso.setVisibilidad(false);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.save(curso)).thenReturn(curso);

        Curso resultado = cursoService.cambiarVisibilidad(10L);

        assertThat(resultado.getVisibilidad()).isTrue();
    }

    // Test para verificar que cambiarVisibilidad lanza RuntimeException 404 cuando el curso no existe
    @Test
    void cambiarVisibilidad_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.cambiarVisibilidad(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que cambiarVisibilidad lanza RuntimeException 403 cuando el usuario no es Maestro
    @Test
    void cambiarVisibilidad_usuarioNoMaestro_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.cambiarVisibilidad(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que cambiarVisibilidad lanza RuntimeException 403 cuando el maestro no es propietario
    @Test
    void cambiarVisibilidad_maestroNoPropietario_lanzaForbidden() {
        Maestro otroMaestro = crearMaestro(99L);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.cambiarVisibilidad(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");

        verify(cursoRepository, never()).save(any());
    }

    // -------------------------------------------------------
    // crearCurso
    // -------------------------------------------------------

    // Test para verificar que crearCurso guarda el curso correctamente cuando el usuario es Maestro
    @Test
    void crearCurso_maestro_guardaCursoConCodigoUnico() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.existsByCodigo(anyString())).thenReturn(false);
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        Curso resultado = cursoService.crearCurso("Física", "Descripción", "img.png");

        assertThat(resultado.getTitulo()).isEqualTo("Física");
        assertThat(resultado.getDescripcion()).isEqualTo("Descripción");
        assertThat(resultado.getImagen()).isEqualTo("img.png");
        assertThat(resultado.getVisibilidad()).isFalse();
        assertThat(resultado.getMaestro()).isEqualTo(maestro);
        assertThat(resultado.getCodigo()).isNotBlank();
        verify(cursoRepository).save(any(Curso.class));
    }

    // Test para verificar que crearCurso reintenta la generación de código si el primero ya existe
    @Test
    void crearCurso_codigoDuplicado_reintentaHastaEncontrarUnico() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.existsByCodigo(anyString()))
                .thenReturn(true)   // primer intento: duplicado
                .thenReturn(false); // segundo intento: libre
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        Curso resultado = cursoService.crearCurso("Química", null, null);

        assertThat(resultado.getCodigo()).isNotBlank();
        verify(cursoRepository).save(any(Curso.class));
    }

    // Test para verificar que crearCurso lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void crearCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.crearCurso("Física", "Desc", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede crear cursos");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que crearCurso guarda correctamente cuando descripción e imagen son null (caso límite)
    @Test
    void crearCurso_descripcionEImagenNull_seGuardaCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.existsByCodigo(anyString())).thenReturn(false);
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        Curso resultado = cursoService.crearCurso("Historia", null, null);

        assertThat(resultado.getDescripcion()).isNull();
        assertThat(resultado.getImagen()).isNull();
    }

    // -------------------------------------------------------
    // actualizarCurso
    // -------------------------------------------------------

    // Test para verificar que actualizarCurso actualiza titulo, descripcion e imagen correctamente cuando el maestro es propietario
    @Test
    void actualizarCurso_maestroPropietario_actualizaCamposCorrectamente() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.save(curso)).thenReturn(curso);

        Curso resultado = cursoService.actualizarCurso(10L, "Nuevo título", "Nueva desc", "nueva.png");

        assertThat(resultado.getTitulo()).isEqualTo("Nuevo título");
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva desc");
        assertThat(resultado.getImagen()).isEqualTo("nueva.png");
        verify(cursoRepository).save(curso);
    }

    // Test para verificar que actualizarCurso lanza RuntimeException 404 cuando el curso no existe
    @Test
    void actualizarCurso_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.actualizarCurso(99L, "T", "D", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que actualizarCurso lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void actualizarCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.actualizarCurso(10L, "T", "D", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar cursos");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que actualizarCurso lanza AccessDeniedException cuando el maestro no es propietario del curso
    @Test
    void actualizarCurso_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otroMaestro = crearMaestro(99L);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.actualizarCurso(10L, "T", "D", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el propietario del curso puede actualizarlo");

        verify(cursoRepository, never()).save(any());
    }

    // -------------------------------------------------------
    // calcularTotalPuntosCursoPorAlumno
    // -------------------------------------------------------

    // Test para verificar que calcularTotalPuntosCursoPorAlumno retorna mapa vacío cuando el curso no tiene inscripciones
    @Test
    void calcularTotalPuntosCursoPorAlumno_sinInscripciones_retornaMapaVacio() {
        curso.setInscripciones(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<Alumno, Integer> resultado = cursoService.calcularTotalPuntosCursoPorAlumno(curso);

        assertThat(resultado).isEmpty();
    }

    // Test para verificar que calcularTotalPuntosCursoPorAlumno retorna 0 puntos cuando el alumno no tiene actividades terminadas
    @Test
    void calcularTotalPuntosCursoPorAlumno_alumnoSinActividadesTerminadas_retornaCeroPuntos() {
        Inscripcion inscripcion = new Inscripcion(0, LocalDate.now(), alumno, curso);
        curso.setInscripciones(List.of(inscripcion));
        curso.setTemas(new ArrayList<>());
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<Alumno, Integer> resultado = cursoService.calcularTotalPuntosCursoPorAlumno(curso);

        assertThat(resultado).containsEntry(alumno, 0);
    }

    // Test para verificar que calcularTotalPuntosCursoPorAlumno suma correctamente los puntos de actividades terminadas
    // getEstadoActividad() es calculado: para que devuelva TERMINADA necesita respuestasAlumno con última respuesta correcta
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

        Map<Alumno, Integer> resultado = cursoService.calcularTotalPuntosCursoPorAlumno(curso);

        assertThat(resultado).containsEntry(alumno, 50);
    }

    // Test para verificar que calcularTotalPuntosCursoPorAlumno lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void calcularTotalPuntosCursoPorAlumno_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.calcularTotalPuntosCursoPorAlumno(curso))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede visualizar los puntos de los alumnos");
    }

    // Test para verificar que calcularTotalPuntosCursoPorAlumno lanza AccessDeniedException cuando el maestro no es propietario
    @Test
    void calcularTotalPuntosCursoPorAlumno_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otroMaestro = crearMaestro(99L);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.calcularTotalPuntosCursoPorAlumno(curso))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro propietario del curso puede visualizar los puntos de los alumnos");
    }

    // -------------------------------------------------------
    // getProgreso
    // -------------------------------------------------------

    // Test para verificar que getProgreso retorna SIN_EMPEZAR cuando el curso no tiene actividades
    @Test
    void getProgreso_cursoSinActividades_retornaSinEmpezar() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(0L);

        ProgresoDTO resultado = cursoService.getProgreso(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que getProgreso retorna SIN_EMPEZAR cuando el alumno no tiene registros de actividad
    @Test
    void getProgreso_alumnoSinRegistros_retornaSinEmpezar() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(3L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of());

        ProgresoDTO resultado = cursoService.getProgreso(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que getProgreso retorna TERMINADA cuando todas las actividades están acabadas
    // ActividadAlumnoProgreso es una interfaz: se implementa con clase anónima inline
    @Test
    void getProgreso_todasActividadesAcabadas_retornaTerminada() {
        LocalDateTime ahora = LocalDateTime.now();
        ActividadAlumnoProgreso progreso = crearProgreso(ahora, ahora);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(1L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso));

        ProgresoDTO resultado = cursoService.getProgreso(10L);

        assertThat(resultado.getEstado()).isEqualTo("TERMINADA");
    }

    // Test para verificar que getProgreso retorna EMPEZADA cuando hay actividades con inicio pero sin acabar todas
    @Test
    void getProgreso_algunaActividadConInicio_retornaEmpezada() {
        ActividadAlumnoProgreso progreso = crearProgreso(LocalDateTime.now(), null);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(2L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso));

        ProgresoDTO resultado = cursoService.getProgreso(10L);

        assertThat(resultado.getEstado()).isEqualTo("EMPEZADA");
    }

    // Test para verificar que getProgreso retorna SIN_EMPEZAR cuando hay registros pero ninguno con inicio ni fin
    @Test
    void getProgreso_registrosSinInicioNiFin_retornaSinEmpezar() {
        ActividadAlumnoProgreso progreso = crearProgreso(null, null);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(2L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso));

        ProgresoDTO resultado = cursoService.getProgreso(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que getProgreso lanza RuntimeException 404 cuando el curso no existe
    @Test
    void getProgreso_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.getProgreso(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");
    }

    // Test para verificar que getProgreso lanza RuntimeException 403 cuando el usuario no es Alumno
    @Test
    void getProgreso_usuarioNoAlumno_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> cursoService.getProgreso(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // -------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------

    // Crea un Maestro de prueba con el id proporcionado
    private static Maestro crearMaestro(Long id) {
        Maestro m = new Maestro();
        m.setId(id);
        return m;
    }

    // Crea un Alumno de prueba con el id proporcionado
    private static Alumno crearAlumno(Long id) {
        Alumno a = new Alumno();
        a.setId(id);
        a.setPuntos(0);
        return a;
    }

    // Crea un Curso de prueba con los datos proporcionados
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

    // Crea una Actividad abstracta de prueba con la puntuación proporcionada
    private static Actividad crearActividad(int puntuacion) {
        Actividad a = new Actividad() {};
        a.setPuntuacion(puntuacion);
        a.setActividadesAlumno(new ArrayList<>());
        return a;
    }

    // Crea un ActividadAlumno con estado TERMINADA para un alumno dado.
    // getEstadoActividad() es calculado: requiere al menos una RespuestaAlumno con correcta=true como última respuesta.
    private static ActividadAlumno crearActividadAlumnoTerminada(Alumno alumno) {
        RespuestaAlumno respuestaCorrecta = new RespuestaAlumno() {};
        respuestaCorrecta.setCorrecta(true);

        ActividadAlumno aa = new ActividadAlumno();
        aa.setAlumno(alumno);
        aa.setTiempo(0);
        aa.setPuntuacion(0);
        aa.setNota(10);
        aa.setInicio(LocalDateTime.now());
        aa.setAcabada(LocalDateTime.now());
        aa.setNumAbandonos(0);
        aa.setRespuestasAlumno(new ArrayList<>(List.of(respuestaCorrecta)));
        return aa;
    }

    // Crea un ActividadAlumnoProgreso (interfaz) con las fechas de inicio y fin proporcionadas usando clase anónima
    private static ActividadAlumnoProgreso crearProgreso(LocalDateTime inicio, LocalDateTime acabada) {
        return new ActividadAlumnoProgreso() {
            @Override
            public LocalDateTime getInicio() { return inicio; }
            @Override
            public LocalDateTime getAcabada() { return acabada; }
        };
    }
}
