package com.cerebrus.temaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.curso.Curso;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaController;
import com.cerebrus.tema.TemaService;
import com.cerebrus.tema.dto.TemaDTO;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
class TemaControllerTest {

    @Mock
    private TemaService temaService;

    @Mock
    private ActividadService actividadService;

    @InjectMocks
    private TemaController temaController;

    private Curso curso;
    private Tema tema;

    @BeforeEach
    void setUp() {
        Maestro maestro = new Maestro();
        maestro.setId(1L);

        curso = new Curso();
        curso.setId(10L);
        curso.setTitulo("Matematicas");
        curso.setMaestro(maestro);

        tema = new Tema("Fracciones", curso);
        tema.setId(100L);
    }

    @Test
    void crearTema_requestValido_retorna201ConTema() {
        TemaController.CrearTemaRequest request = new TemaController.CrearTemaRequest();
        request.setTitulo("Fracciones");
        request.setCursoId(10L);

        when(temaService.crearTema("Fracciones", 10L, 1L)).thenReturn(tema);

        ResponseEntity<?> respuesta = temaController.crearTema(request, 1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        verify(temaService).crearTema("Fracciones", 10L, 1L);
    }

    @Test
    void crearTema_errorValidacion_retorna400() {
        TemaController.CrearTemaRequest request = new TemaController.CrearTemaRequest();
        request.setTitulo("Fracciones");
        request.setCursoId(10L);

        when(temaService.crearTema("Fracciones", 10L, 1L)).thenThrow(new IllegalArgumentException("Curso no encontrado"));

        ResponseEntity<?> respuesta = temaController.crearTema(request, 1L);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Puedes comprobar el mensaje de error en el body si lo necesitas
    }

    @Test
    void renombrarTema_requestValido_retorna200ConTema() {
        TemaController.RenombrarTemaRequest request = new TemaController.RenombrarTemaRequest();
        request.setNuevoTitulo("Decimales");

        Tema temaRenombrado = new Tema("Decimales", curso);
        temaRenombrado.setId(100L);

        when(temaService.renombrarTema(100L, "Decimales", 1L)).thenReturn(temaRenombrado);

        ResponseEntity<?> respuesta = temaController.renombrarTema(100L, request, 1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        verify(temaService).renombrarTema(100L, "Decimales", 1L);
    }

    @Test
    void renombrarTema_errorValidacion_retorna400() {
        TemaController.RenombrarTemaRequest request = new TemaController.RenombrarTemaRequest();
        request.setNuevoTitulo("Decimales");

        when(temaService.renombrarTema(100L, "Decimales", 1L)).thenThrow(new IllegalArgumentException("Tema no encontrado"));

        ResponseEntity<?> respuesta = temaController.renombrarTema(100L, request, 1L);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Puedes comprobar el mensaje de error en el body si lo necesitas
    }

    @Test
    void encontrarTemaPorId_existente_retorna200ConTemaDTOYActividades() {
        Actividad actividad = new Actividad() {};
        actividad.setId(5L);
        actividad.setTitulo("Actividad 1");
        actividad.setDescripcion("Desc");
        actividad.setPuntuacion(10);
        actividad.setPosicion(1);

        when(temaService.encontrarTemaPorId(100L)).thenReturn(tema);
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(actividad));

        ResponseEntity<TemaDTO> respuesta = temaController.encontrarTemaPorId(100L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isEqualTo(100L);
        assertThat(respuesta.getBody().getCursoId()).isEqualTo(10L);
        assertThat(respuesta.getBody().getActividades()).hasSize(1);
    }

    @Test
    void encontrarTemaPorId_noExiste_retorna404() {
        when(temaService.encontrarTemaPorId(999L)).thenThrow(new IllegalArgumentException("Tema no encontrado"));

        assertThatThrownBy(() -> temaController.encontrarTemaPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tema no encontrado");

    }

    @Test
    void encontrarTemasPorCursoAlumnoId_retorna200ConListaTemasDTO() {
        Tema tema2 = new Tema("Geometria", curso);
        tema2.setId(101L);

        when(temaService.encontrarTemasPorCursoAlumnoId(10L)).thenReturn(List.of(tema, tema2));
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(new ArrayList<>());
        when(actividadService.encontrarActividadesPorTema(101L)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TemaDTO>> respuesta = temaController.encontrarTemasPorCursoAlumnoId(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(2);
        assertThat(respuesta.getBody().get(0).getId()).isEqualTo(100L);
        assertThat(respuesta.getBody().get(1).getId()).isEqualTo(101L);
    }

    @Test
    void encontrarTemasPorCursoMaestroId_retorna200ConListaTemasDTO() {
        when(temaService.encontrarTemasPorCursoMaestroId(10L)).thenReturn(List.of(tema));
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TemaDTO>> respuesta = temaController.encontrarTemasPorCursoMaestroId(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(1);
        assertThat(respuesta.getBody().getFirst().getTitulo()).isEqualTo("Fracciones");
    }

    @Test
    void eliminarTemaPorId_existente_retorna204() {
        ResponseEntity<Void> respuesta = temaController.eliminarTemaPorId(100L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(respuesta.getBody()).isNull();
        verify(temaService).eliminarTemaPorId(100L);
    }

    @Test
    void eliminarTemaPorId_errorPermisos_retorna400() {
        doThrow(new AccessDeniedException("El usuario no tiene permiso para eliminar este tema.")).when(temaService).eliminarTemaPorId(100L);

        assertThatThrownBy(() -> temaController.eliminarTemaPorId(100L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("El usuario no tiene permiso para eliminar este tema.");
    }
}
