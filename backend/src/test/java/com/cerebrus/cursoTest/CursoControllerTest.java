package com.cerebrus.cursoTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoController;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.curso.ProgresoDTO;
import com.cerebrus.estadisticas.EstadisticasMaestroController;
import com.cerebrus.estadisticas.EstadisticasMaestroServiceImpl;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Maestro;

@ExtendWith(MockitoExtension.class)
class CursoControllerTest {

    @Mock
    private CursoServiceImpl cursoService;

    @InjectMocks
    private CursoController cursoController;

    @Mock
    private EstadisticasMaestroServiceImpl estadisticasMaestroService;

    @InjectMocks
    private EstadisticasMaestroController estadisticasMaestroController;

    private Maestro maestro;
    private Curso curso;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        maestro.setId(1L);

        curso = new Curso();
        curso.setId(10L);
        curso.setTitulo("Matemáticas");
        curso.setVisibilidad(true);
        curso.setMaestro(maestro);
        curso.setCodigo("ABC1234");
    }

    // -------------------------------------------------------
    // GET /{id}/detalles - obtenerDetallesCurso
    // -------------------------------------------------------

    // Test para verificar que obtenerDetallesCurso retorna 200 OK con la lista de detalles del curso
    @Test
    void obtenerDetallesCurso_cursoExistente_retorna200ConDetalles() {
        List<String> detalles = List.of("Matemáticas", "Descripción", "img.png", "ABC1234");
        when(cursoService.obtenerDetallesCurso(10L)).thenReturn(detalles);

        ResponseEntity<List<String>> respuesta = cursoController.obtenerDetallesCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).containsExactly("Matemáticas", "Descripción", "img.png", "ABC1234");
    }

    // Test para verificar que obtenerDetallesCurso retorna 404 cuando el service lanza RuntimeException con mensaje 404
    @Test
    void obtenerDetallesCurso_cursoNoExiste_retorna404() {
        when(cursoService.obtenerDetallesCurso(99L)).thenThrow(new RuntimeException("404 Not Found"));

        ResponseEntity<List<String>> respuesta = cursoController.obtenerDetallesCurso(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Test para verificar que obtenerDetallesCurso retorna 403 cuando el service lanza RuntimeException con mensaje 403
    @Test
    void obtenerDetallesCurso_accesoNoPermitido_retorna403() {
        when(cursoService.obtenerDetallesCurso(10L)).thenThrow(new RuntimeException("403 Forbidden"));

        ResponseEntity<List<String>> respuesta = cursoController.obtenerDetallesCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Test para verificar que obtenerDetallesCurso retorna 500 cuando el service lanza una RuntimeException inesperada
    @Test
    void obtenerDetallesCurso_errorInesperado_retorna500() {
        when(cursoService.obtenerDetallesCurso(10L)).thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<List<String>> respuesta = cursoController.obtenerDetallesCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------
    // GET /{id}/puntos - obtenerPuntosCurso
    // -------------------------------------------------------

    // Test para verificar que obtenerPuntosCurso retorna 200 OK con el mapa de puntos por alumno
    @Test
    void obtenerPuntosCurso_maestroPropietario_retorna200ConPuntos() {
        Alumno alumno = new Alumno();
        alumno.setId(2L);
        Map<Alumno, Integer> puntos = Map.of(alumno, 100);

        when(cursoService.getCursoById(10L)).thenReturn(curso);
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(curso)).thenReturn(puntos);

        ResponseEntity<Map<Alumno, Integer>> respuesta = estadisticasMaestroController.obtenerPuntosCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).containsEntry(alumno, 100);
    }

    // Test para verificar que obtenerPuntosCurso retorna 403 cuando el service lanza AccessDeniedException
    @Test
    void obtenerPuntosCurso_accesoNoPermitido_retorna403() {
        when(cursoService.getCursoById(10L)).thenReturn(curso);
        when(estadisticasMaestroService.calcularTotalPuntosCursoPorAlumno(curso))
                .thenThrow(new AccessDeniedException("Solo un maestro puede visualizar los puntos de los alumnos"));

        ResponseEntity<Map<Alumno, Integer>> respuesta = estadisticasMaestroController.obtenerPuntosCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Test para verificar que obtenerPuntosCurso retorna 404 cuando el service lanza RuntimeException con mensaje 404
    @Test
    void obtenerPuntosCurso_cursoNoExiste_retorna404() {
        when(cursoService.getCursoById(99L)).thenThrow(new RuntimeException("404 Not Found"));

        ResponseEntity<Map<Alumno, Integer>> respuesta = estadisticasMaestroController.obtenerPuntosCurso(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Test para verificar que obtenerPuntosCurso retorna 500 cuando el service lanza una RuntimeException inesperada
    @Test
    void obtenerPuntosCurso_errorInesperado_retorna500() {
        when(cursoService.getCursoById(10L)).thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<Map<Alumno, Integer>> respuesta = estadisticasMaestroController.obtenerPuntosCurso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------
    // POST /curso - crearCurso
    // -------------------------------------------------------

    // Test para verificar que crearCurso retorna 201 CREATED con el curso creado
    @Test
    void crearCurso_requestValido_retorna201ConCurso() {
        when(cursoService.crearCurso("Física", "Desc", "img.png")).thenReturn(curso);

        CursoController.CrearCursoRequest request = new CursoController.CrearCursoRequest();
        request.setTitulo("Física");
        request.setDescripcion("Desc");
        request.setImagen("img.png");

        ResponseEntity<Curso> respuesta = cursoController.crearCurso(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(10L);
        verify(cursoService).crearCurso("Física", "Desc", "img.png");
    }

    // Test para verificar que crearCurso retorna 201 cuando descripción e imagen son null
    @Test
    void crearCurso_sinDescripcionNiImagen_retorna201() {
        when(cursoService.crearCurso("Historia", null, null)).thenReturn(curso);

        CursoController.CrearCursoRequest request = new CursoController.CrearCursoRequest();
        request.setTitulo("Historia");

        ResponseEntity<Curso> respuesta = cursoController.crearCurso(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // -------------------------------------------------------
    // GET / - obtenerCursos
    // -------------------------------------------------------

    // Test para verificar que obtenerCursos retorna 200 OK con la lista de cursos del usuario logueado
    @Test
    void obtenerCursos_usuarioLogueado_retorna200ConListaCursos() {
        when(cursoService.ObtenerCursosUsuarioLogueado()).thenReturn(List.of(curso));

        ResponseEntity<List<Curso>> respuesta = cursoController.obtenerCursos();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).containsExactly(curso);
    }

    // Test para verificar que obtenerCursos retorna 200 OK con lista vacía cuando el usuario no tiene cursos
    @Test
    void obtenerCursos_usuarioSinCursos_retorna200ConListaVacia() {
        when(cursoService.ObtenerCursosUsuarioLogueado()).thenReturn(new ArrayList<>());

        ResponseEntity<List<Curso>> respuesta = cursoController.obtenerCursos();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEmpty();
    }

    // -------------------------------------------------------
    // PATCH /{id}/visibilidad - cambiarVisibilidad
    // -------------------------------------------------------

    // Test para verificar que cambiarVisibilidad retorna 200 OK con el curso actualizado
    @Test
    void cambiarVisibilidad_maestroPropietario_retorna200ConCursoActualizado() {
        when(cursoService.cambiarVisibilidad(10L)).thenReturn(curso);

        ResponseEntity<Curso> respuesta = cursoController.cambiarVisibilidad(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        verify(cursoService).cambiarVisibilidad(10L);
    }

    // Test para verificar que cambiarVisibilidad retorna 404 cuando el service lanza RuntimeException con mensaje 404
    @Test
    void cambiarVisibilidad_cursoNoExiste_retorna404() {
        when(cursoService.cambiarVisibilidad(99L)).thenThrow(new RuntimeException("404 Not Found"));

        ResponseEntity<Curso> respuesta = cursoController.cambiarVisibilidad(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Test para verificar que cambiarVisibilidad retorna 403 cuando el service lanza RuntimeException con mensaje 403
    @Test
    void cambiarVisibilidad_accesoNoPermitido_retorna403() {
        when(cursoService.cambiarVisibilidad(10L)).thenThrow(new RuntimeException("403 Forbidden"));

        ResponseEntity<Curso> respuesta = cursoController.cambiarVisibilidad(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------
    // GET /{id}/progreso - obtenerProgreso
    // -------------------------------------------------------

    // Test para verificar que obtenerProgreso retorna 200 OK con el ProgresoDTO del alumno
    @Test
    void obtenerProgreso_alumnoInscrito_retorna200ConProgreso() {
        ProgresoDTO dto = new ProgresoDTO("EMPEZADA", 0);
        when(cursoService.getProgreso(10L)).thenReturn(dto);

        ResponseEntity<ProgresoDTO> respuesta = cursoController.obtenerProgreso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getEstado()).isEqualTo("EMPEZADA");
    }

    // Test para verificar que obtenerProgreso retorna 404 cuando el curso no existe
    @Test
    void obtenerProgreso_cursoNoExiste_retorna404() {
        when(cursoService.getProgreso(99L)).thenThrow(new RuntimeException("404 Not Found"));

        ResponseEntity<ProgresoDTO> respuesta = cursoController.obtenerProgreso(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Test para verificar que obtenerProgreso retorna 403 cuando el usuario no es alumno
    @Test
    void obtenerProgreso_usuarioNoAlumno_retorna403() {
        when(cursoService.getProgreso(10L)).thenThrow(new RuntimeException("403 Forbidden"));

        ResponseEntity<ProgresoDTO> respuesta = cursoController.obtenerProgreso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------
    // PATCH /{id} - actualizarCurso
    // -------------------------------------------------------

    // Test para verificar que actualizarCurso retorna 200 OK con el curso actualizado
    @Test
    void actualizarCurso_maestroPropietario_retorna200ConCursoActualizado() {
        when(cursoService.actualizarCurso(10L, "Nuevo título", "Nueva desc", "nueva.png")).thenReturn(curso);

        CursoController.ActualizarCursoRequest request = new CursoController.ActualizarCursoRequest();
        request.setTitulo("Nuevo título");
        request.setDescripcion("Nueva desc");
        request.setImagen("nueva.png");

        ResponseEntity<Curso> respuesta = cursoController.actualizarCurso(10L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cursoService).actualizarCurso(10L, "Nuevo título", "Nueva desc", "nueva.png");
    }

    // Test para verificar que actualizarCurso retorna 400 cuando el id es 0 (caso límite)
    @Test
    void actualizarCurso_idCero_retorna400() {
        CursoController.ActualizarCursoRequest request = new CursoController.ActualizarCursoRequest();
        request.setTitulo("Título");

        ResponseEntity<Curso> respuesta = cursoController.actualizarCurso(0L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Test para verificar que actualizarCurso retorna 403 cuando el service lanza AccessDeniedException
    @Test
    void actualizarCurso_accesoNoPermitido_retorna403() {
        when(cursoService.actualizarCurso(anyLong(), any(), any(), any()))
                .thenThrow(new AccessDeniedException("Solo un maestro puede actualizar cursos"));

        CursoController.ActualizarCursoRequest request = new CursoController.ActualizarCursoRequest();
        request.setTitulo("Título");

        ResponseEntity<Curso> respuesta = cursoController.actualizarCurso(10L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Test para verificar que actualizarCurso retorna 404 cuando el service lanza RuntimeException con mensaje 404
    @Test
    void actualizarCurso_cursoNoExiste_retorna404() {
        when(cursoService.actualizarCurso(anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("404 Not Found"));

        CursoController.ActualizarCursoRequest request = new CursoController.ActualizarCursoRequest();
        request.setTitulo("Título");

        ResponseEntity<Curso> respuesta = cursoController.actualizarCurso(99L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Test para verificar que actualizarCurso retorna 500 cuando el service lanza una RuntimeException inesperada
    @Test
    void actualizarCurso_errorInesperado_retorna500() {
        when(cursoService.actualizarCurso(anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error interno"));

        CursoController.ActualizarCursoRequest request = new CursoController.ActualizarCursoRequest();
        request.setTitulo("Título");

        ResponseEntity<Curso> respuesta = cursoController.actualizarCurso(10L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
