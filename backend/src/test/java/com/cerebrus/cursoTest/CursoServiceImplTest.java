package com.cerebrus.cursoTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import com.cerebrus.actividadAlumn.ActividadAlumnoProgreso;
import com.cerebrus.actividadAlumn.ActividadAlumnoRepository;
import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoRepository;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.curso.dto.ProgresoDTO;
import com.cerebrus.estadisticas.EstadisticasMaestroServiceImpl;
import com.cerebrus.respuestaAlumn.RespuestaAlumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EstadisticasMaestroServiceImpl estadisticasMaestroService;

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
    // encontrarCursosPorUsuarioLogueado
    // -------------------------------------------------------

    // Test para verificar que encontrarCursosPorUsuarioLogueado retorna los cursos del maestro logueado
    @Test
    void encontrarCursosPorUsuarioLogueado_maestro_retornaCursosPropios() {
        List<Curso> cursosMaestro = List.of(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.findByMaestroId(1L)).thenReturn(cursosMaestro);

        List<Curso> resultado = cursoService.encontrarCursosPorUsuarioLogueado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(curso);
        verify(cursoRepository).findByMaestroId(1L);
    }


    // Test para verificar que encontrarCursosPorUsuarioLogueado retorna los cursos visibles del alumno logueado
    @Test
    void encontrarCursosPorUsuarioLogueado_alumno_retornaCursosInscritos() {
        List<Curso> cursosAlumno = List.of(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(cursosAlumno);

        List<Curso> resultado = cursoService.encontrarCursosPorUsuarioLogueado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(curso);
        verify(cursoRepository).findByAlumnoId(2L);
    }

    // Test para verificar que encontrarCursosPorUsuarioLogueado lanza RuntimeException cuando el usuario no es ni Maestro ni Alumno
    @Test
    void encontrarCursosPorUsuarioLogueado_usuarioGenerico_lanzaRuntimeException() {
        when(usuarioService.findCurrentUser()).thenReturn(usuarioGenerico);

        assertThatThrownBy(() -> cursoService.encontrarCursosPorUsuarioLogueado())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // -------------------------------------------------------
    // encontrarDetallesCursoPorId
    // -------------------------------------------------------

    // Test para verificar que encontrarDetallesCursoPorId retorna titulo, descripcion, imagen y codigo para el maestro propietario
    @Test
    void encontrarDetallesCursoPorId_maestroPropietario_retornaDetallesConCodigo() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        List<String> resultado = cursoService.encontrarDetallesCursoPorId(10L);

        assertThat(resultado).containsExactly("Matemáticas", curso.getDescripcion(), curso.getImagen(), curso.getCodigo());
    }

    // Test para verificar que encontrarDetallesCursoPorId lanza RuntimeException 403 cuando el maestro no es propietario del curso
    @Test
    void encontrarDetallesCursoPorId_maestroNoPropietario_lanzaForbidden() {
        Maestro otroMaestro = crearMaestro(99L);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.encontrarDetallesCursoPorId(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que encontrarDetallesCursoPorId retorna titulo, descripcion e imagen para alumno inscrito en curso visible
    @Test
    void encontrarDetallesCursoPorId_alumnoInscritoYVisible_retornaDetallesSinCodigo() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of(curso));

        List<String> resultado = cursoService.encontrarDetallesCursoPorId(10L);

        assertThat(resultado).containsExactly("Matemáticas", curso.getDescripcion(), curso.getImagen());
        assertThat(resultado).doesNotContain(curso.getCodigo());
    }

    // Test para verificar que encontrarDetallesCursoPorId lanza RuntimeException 403 cuando el alumno está inscrito pero el curso no es visible
    @Test
    void encontrarDetallesCursoPorId_alumnoInscritoYNoVisible_lanzaForbidden() {
        curso.setVisibilidad(false);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of(curso));

        assertThatThrownBy(() -> cursoService.encontrarDetallesCursoPorId(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que encontrarDetallesCursoPorId lanza RuntimeException 403 cuando el alumno no está inscrito en el curso
    @Test
    void encontrarDetallesCursoPorId_alumnoNoInscrito_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(cursoRepository.findByAlumnoId(2L)).thenReturn(List.of());

        assertThatThrownBy(() -> cursoService.encontrarDetallesCursoPorId(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // Test para verificar que encontrarDetallesCursoPorId lanza RuntimeException 404 cuando el curso no existe
    @Test
    void encontrarDetallesCursoPorId_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.encontrarDetallesCursoPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");
    }

    // Test para verificar que encontrarDetallesCursoPorId lanza RuntimeException 403 cuando el usuario no es ni Maestro ni Alumno
    @Test
    void encontrarDetallesCursoPorId_usuarioGenerico_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(usuarioGenerico);

        assertThatThrownBy(() -> cursoService.encontrarDetallesCursoPorId(10L))
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

        Curso resultado = cursoService.crearCurso("Física", "Descripción", "img.png", "CODIGO");

        assertThat(resultado.getTitulo()).isEqualTo("Física");
        assertThat(resultado.getDescripcion()).isEqualTo("Descripción");
        assertThat(resultado.getImagen()).isEqualTo("img.png");
        assertThat(resultado.getVisibilidad()).isFalse();
        assertThat(resultado.getMaestro()).isEqualTo(maestro);
        assertThat(resultado.getCodigo()).isNotBlank();
        verify(cursoRepository).save(any(Curso.class));
    }

    // Test para verificar que crearCurso reintenta la generación de código si el primero ya existe
    // @Test
    // void crearCurso_codigoDuplicado_reintentaHastaEncontrarUnico() {
    //     when(usuarioService.findCurrentUser()).thenReturn(maestro);
    //     when(cursoRepository.existsByCodigo(anyString()))
    //             .thenReturn(true)   // primer intento: duplicado
    //             .thenReturn(false); // segundo intento: libre
    //     when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

    //     Curso resultado = cursoService.crearCurso("Química", null, null, "CODIGO123");

    //     assertThat(resultado.getCodigo()).isNotBlank();
    //     verify(cursoRepository).save(any(Curso.class));
    // }

    // Test para verificar que crearCurso lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void crearCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.crearCurso("Física", "Desc", null, "CODIGO1234"))
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

        Curso resultado = cursoService.crearCurso("Historia", null, null, "CODIGO12345");

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

        Curso resultado = cursoService.actualizarCurso(10L, "Nuevo título", "Nueva desc", "nueva.png", "nuevo-codigo");

        assertThat(resultado.getTitulo()).isEqualTo("Nuevo título");
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva desc");
        assertThat(resultado.getImagen()).isEqualTo("nueva.png");
        assertThat(resultado.getCodigo()).isEqualTo("nuevo-codigo");
        verify(cursoRepository).save(curso);
    }

    // Test para verificar que actualizarCurso lanza RuntimeException 404 cuando el curso no existe
    @Test
    void actualizarCurso_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.actualizarCurso(99L, "T", "D", null, "CODIGO123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que actualizarCurso lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void actualizarCurso_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.actualizarCurso(10L, "T", "D", null, "CODIGO123"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede actualizar cursos");

        verify(cursoRepository, never()).save(any());
    }

    // Test para verificar que actualizarCurso actualiza correctamente cuando descripcion e imagen son null
    @Test
    void actualizarCurso_descripcionEImagenNull_actualizaCorrectamente() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(cursoRepository.save(curso)).thenReturn(curso);

        Curso resultado = cursoService.actualizarCurso(10L, "Nuevo título", null, null, "nuevo-codigo");

        assertThat(resultado.getTitulo()).isEqualTo("Nuevo título");
        assertThat(resultado.getDescripcion()).isNull();
        assertThat(resultado.getImagen()).isNull();
        assertThat(resultado.getCodigo()).isEqualTo("nuevo-codigo");
    }

    // -------------------------------------------------------
    // encontrarProgresoPorCursoId
    // -------------------------------------------------------

    // Test para verificar que encontrarProgresoPorCursoId retorna SIN_EMPEZAR cuando el curso no tiene actividades
    @Test
    void encontrarProgresoPorCursoId_cursoSinActividades_retornaSinEmpezar() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(0L);

        ProgresoDTO resultado = cursoService.encontrarProgresoPorCursoId(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que encontrarProgresoPorCursoId retorna SIN_EMPEZAR cuando el alumno no tiene registros de actividad
    @Test
    void encontrarProgresoPorCursoId_alumnoSinRegistros_retornaSinEmpezar() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(3L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of());

        ProgresoDTO resultado = cursoService.encontrarProgresoPorCursoId(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que encontrarProgresoPorCursoId retorna TERMINADA con puntos totales cuando todas las actividades están acabadas
    @Test
    void encontrarProgresoPorCursoId_todasActividadesAcabadas_conPuntos_retornaTerminadaConPuntos() {
        LocalDateTime ahora = LocalDateTime.now();
        ActividadAlumnoProgreso progreso1 = new ActividadAlumnoProgreso() {
            @Override public Long getActividadId() { return 1L; }
            @Override public LocalDateTime getInicio() { return ahora; }
            @Override public LocalDateTime getAcabada() { return ahora; }
            @Override public Integer getPuntuacion() { return 10; }
        };
        ActividadAlumnoProgreso progreso2 = new ActividadAlumnoProgreso() {
            @Override public Long getActividadId() { return 2L; }
            @Override public LocalDateTime getInicio() { return ahora; }
            @Override public LocalDateTime getAcabada() { return ahora; }
            @Override public Integer getPuntuacion() { return 20; }
        };

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(2L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso1, progreso2));

        ProgresoDTO resultado = cursoService.encontrarProgresoPorCursoId(10L);

        assertThat(resultado.getEstado()).isEqualTo("TERMINADA");
        assertThat(resultado.getPuntos()).isEqualTo(30); // 10 + 20
    }

    // Test para verificar que encontrarProgresoPorCursoId retorna EMPEZADA cuando hay actividades con inicio pero sin acabar todas
    @Test
    void encontrarProgresoPorCursoId_algunaActividadConInicio_retornaEmpezada() {
        ActividadAlumnoProgreso progreso = crearProgreso(LocalDateTime.now(), null);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(2L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso));

        ProgresoDTO resultado = cursoService.encontrarProgresoPorCursoId(10L);

        assertThat(resultado.getEstado()).isEqualTo("EMPEZADA");
    }

    // Test para verificar que encontrarProgresoPorCursoId retorna SIN_EMPEZAR cuando hay registros pero ninguno con inicio ni fin
    @Test
    void encontrarProgresoPorCursoId_registrosSinInicioNiFin_retornaSinEmpezar() {
        ActividadAlumnoProgreso progreso = crearProgreso(null, null);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);
        when(actividadRepository.countByCursoId(10L)).thenReturn(2L);
        when(actividadAlumnoRepository.findProgresoByAlumnoAndCursoId(alumno, 10L)).thenReturn(List.of(progreso));

        ProgresoDTO resultado = cursoService.encontrarProgresoPorCursoId(10L);

        assertThat(resultado.getEstado()).isEqualTo("SIN_EMPEZAR");
    }

    // Test para verificar que encontrarProgresoPorCursoId lanza RuntimeException 404 cuando el curso no existe
    @Test
    void encontrarProgresoPorCursoId_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.encontrarProgresoPorCursoId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");
    }

    // Test para verificar que encontrarProgresoPorCursoId lanza RuntimeException 403 cuando el usuario no es Alumno
    @Test
    void encontrarProgresoPorCursoId_usuarioNoAlumno_lanzaForbidden() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> cursoService.encontrarProgresoPorCursoId(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("403 Forbidden");
    }

    // -------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------

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
        aa.setPuntuacion(0);
        aa.setNota(10);
        aa.setFechaInicio(LocalDateTime.now());
        aa.setFechaFin(LocalDateTime.now());
        aa.setNumAbandonos(0);
        aa.setRespuestasAlumno(new ArrayList<>(List.of(respuestaCorrecta)));
        return aa;
    }

    // -------------------------------------------------------
    // encontrarCursoPorId
    // -------------------------------------------------------

    // Test para verificar que encontrarCursoPorId retorna el curso cuando existe
    @Test
    void encontrarCursoPorId_cursoExiste_retornaCurso() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);

        Curso resultado = cursoService.encontrarCursoPorId(10L);

        assertThat(resultado).isEqualTo(curso);
    }

    // Test para verificar que encontrarCursoPorId retorna null cuando el curso no existe
    @Test
    void encontrarCursoPorId_cursoNoExiste_retornaNull() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        Curso resultado = cursoService.encontrarCursoPorId(99L);

        assertThat(resultado).isNull();
    }

    // -------------------------------------------------------
    // eliminarCursoPorId
    // -------------------------------------------------------

    // Test para verificar que eliminarCursoPorId elimina el curso correctamente cuando el maestro es propietario
    @Test
    void eliminarCursoPorId_maestroPropietario_eliminaCurso() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        cursoService.eliminarCursoPorId(10L);

        verify(cursoRepository).delete(curso);
    }

    // Test para verificar que eliminarCursoPorId lanza RuntimeException 404 cuando el curso no existe
    @Test
    void eliminarCursoPorId_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.eliminarCursoPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");

        verify(cursoRepository, never()).delete(any());
    }

    // Test para verificar que eliminarCursoPorId lanza AccessDeniedException cuando el usuario no es Maestro
    @Test
    void eliminarCursoPorId_usuarioNoMaestro_lanzaAccessDeniedException() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> cursoService.eliminarCursoPorId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo un maestro puede eliminar cursos");

        verify(cursoRepository, never()).delete(any());
    }

    // Test para verificar que eliminarCursoPorId lanza AccessDeniedException cuando el maestro no es propietario del curso
    @Test
    void eliminarCursoPorId_maestroNoPropietario_lanzaAccessDeniedException() {
        Maestro otroMaestro = crearMaestro(99L);
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(otroMaestro);

        assertThatThrownBy(() -> cursoService.eliminarCursoPorId(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo el propietario puede eliminar este curso");

        verify(cursoRepository, never()).delete(any());
    }

    // -------------------------------------------------------
    // obtenerNotaMediaPorActividadPorCursoId
    // -------------------------------------------------------

    // Test para verificar que obtenerNotaMediaPorActividadPorCursoId calcula correctamente las notas medias cuando el maestro es propietario
    @Test
    void obtenerNotaMediaPorActividadPorCursoId_maestroPropietario_retornaNotasMedias() {
        Actividad actividad1 = crearActividad(10);
        actividad1.setId(100L);
        Actividad actividad2 = crearActividad(20);
        actividad2.setId(101L);

        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setActividad(actividad1);
        aa1.setNota(8);

        ActividadAlumno aa2 = crearActividadAlumnoTerminada(alumno);
        aa2.setActividad(actividad1);
        aa2.setNota(10);

        ActividadAlumno aa3 = crearActividadAlumnoTerminada(alumno);
        aa3.setActividad(actividad2);
        aa3.setNota(7);

        List<ActividadAlumno> actividades = List.of(aa1, aa2, aa3);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadAlumnoRepository.findByCursoID(10L)).thenReturn(actividades);

        List<Integer> resultado = cursoService.obtenerNotaMediaPorActividadPorCursoId(10L);

        assertThat(resultado).containsExactly(9, 7); // (8+10)/2 = 9, 7/1 = 7
    }

    // Test para verificar que obtenerNotaMediaPorActividadPorCursoId retorna lista vacía cuando no hay actividades
    @Test
    void obtenerNotaMediaPorActividadPorCursoId_sinActividades_retornaListaVacia() {
        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadAlumnoRepository.findByCursoID(10L)).thenReturn(List.of());

        List<Integer> resultado = cursoService.obtenerNotaMediaPorActividadPorCursoId(10L);

        assertThat(resultado).isEmpty();
    }

    // Test para verificar que obtenerNotaMediaPorActividadPorCursoId lanza RuntimeException 404 cuando el curso no existe
    @Test
    void obtenerNotaMediaPorActividadPorCursoId_cursoNoExiste_lanzaNotFound() {
        when(cursoRepository.findByID(99L)).thenReturn(null);

        assertThatThrownBy(() -> cursoService.obtenerNotaMediaPorActividadPorCursoId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("404 Not Found");
    }

    // Test para verificar que obtenerNotaMediaPorActividadPorCursoId retorna 0 cuando hay actividades pero sin notas
    @Test
    void obtenerNotaMediaPorActividadPorCursoId_actividadesSinNotas_retornaCero() {
        Actividad actividad1 = crearActividad(10);
        actividad1.setId(100L);

        ActividadAlumno aa1 = crearActividadAlumnoTerminada(alumno);
        aa1.setActividad(actividad1);
        aa1.setNota(null); // Sin nota

        List<ActividadAlumno> actividades = List.of(aa1);

        when(cursoRepository.findByID(10L)).thenReturn(curso);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        when(actividadAlumnoRepository.findByCursoID(10L)).thenReturn(actividades);

        List<Integer> resultado = cursoService.obtenerNotaMediaPorActividadPorCursoId(10L);

        assertThat(resultado).containsExactly(0);
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

    // Crea un ActividadAlumnoProgreso (interfaz) con las fechas de inicio y fin proporcionadas usando clase anónima
    private static ActividadAlumnoProgreso crearProgreso(LocalDateTime inicio, LocalDateTime acabada) {
        return new ActividadAlumnoProgreso() {
            @Override
            public Long getActividadId() { return 1L; }
            @Override
            public LocalDateTime getInicio() { return inicio; }
            @Override
            public LocalDateTime getAcabada() { return acabada; }
            @Override
            public Integer getPuntuacion() { return 0; }
        };
    }
}