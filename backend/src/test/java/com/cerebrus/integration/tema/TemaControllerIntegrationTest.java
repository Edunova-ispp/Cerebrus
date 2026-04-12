package com.cerebrus.integration.tema;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.curso.Curso;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaController;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.maestro.Maestro;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TemaControllerIntegrationTest {

    @Mock
    private TemaService temaService;

    @Mock
    private ActividadService actividadService;

    @InjectMocks
    private TemaController temaController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Tema temaFracciones;
    private Tema temaGeometria;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(temaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Curso curso = fixtureCurso(10L, 1L);
        temaFracciones = new Tema("Fracciones", curso);
        temaFracciones.setId(100L);

        temaGeometria = new Tema("Geometria", curso);
        temaGeometria.setId(101L);
    }

    // -------------------------------------------------------
    // PROFESOR - Creacion de temas
    // -------------------------------------------------------

    @Test
    void crearTema_profesorPropietario_devuelve201() throws Exception {
        when(temaService.crearTema("Fracciones", 10L, 1L)).thenReturn(temaFracciones);

        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.titulo").value("Fracciones"))
                .andExpect(jsonPath("$.cursoId").value(10L))
                .andExpect(jsonPath("$.actividades").isArray())
                .andExpect(jsonPath("$.actividades.length()").value(0));

        verify(temaService).crearTema("Fracciones", 10L, 1L);
    }

    @Test
    void crearTema_profesorNoPropietario_devuelve400() throws Exception {
        when(temaService.crearTema("Fracciones", 10L, 2L))
                .thenThrow(new IllegalArgumentException("El maestro no es propietario del curso"));

        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearTema_tituloVacio_devuelve422YNoLlamaServicio() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload(" ", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());

        verify(temaService, never()).crearTema(any(), any(), any());
    }

    @Test
    void crearTema_cursoIdNulo_devuelve422YNoLlamaServicio() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", null));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());

        verify(temaService, never()).crearTema(any(), any(), any());
    }

    @Test
    void crearTema_sinMaestroId_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(temaService, never()).crearTema(any(), any(), any());
    }

    // -------------------------------------------------------
    // PROFESOR - Edicion de temas
    // -------------------------------------------------------

    @Test
    void editarTema_profesorPropietario_devuelve200() throws Exception {
        Tema temaRenombrado = new Tema("Decimales", temaFracciones.getCurso());
        temaRenombrado.setId(100L);
        when(temaService.renombrarTema(100L, "Decimales", 1L)).thenReturn(temaRenombrado);
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of());

        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Decimales"));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.titulo").value("Decimales"))
                .andExpect(jsonPath("$.cursoId").value(10L));

        verify(temaService).renombrarTema(100L, "Decimales", 1L);
    }

    @Test
    void editarTema_profesorNoPropietario_devuelve403() throws Exception {
        when(temaService.renombrarTema(100L, "Decimales", 2L))
                .thenThrow(new AccessDeniedException("El maestro no es propietario de este tema"));

        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Decimales"));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void editarTema_tituloVacio_devuelve422YNoPersisteCambios() throws Exception {
        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload(""));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());

        verify(temaService, never()).renombrarTema(any(), any(), any());
    }

    @Test
    void editarTema_tituloSoloEspacios_devuelve422YNoPersisteCambios() throws Exception {
        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("   "));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());

        verify(temaService, never()).renombrarTema(any(), any(), any());
    }

    @Test
    void editarTema_temaNoExiste_devuelve400() throws Exception {
        when(temaService.renombrarTema(999L, "Nuevo", 1L))
                .thenThrow(new IllegalArgumentException("Tema no encontrado"));

        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Nuevo"));

        mockMvc.perform(put("/api/temas/999")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editarTema_maestroIdNoNumerico_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Nuevo titulo"));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(temaService, never()).renombrarTema(any(), any(), any());
    }

    // -------------------------------------------------------
    // PROFESOR - Eliminacion de temas
    // -------------------------------------------------------

    @Test
    void eliminarTema_profesorPropietario_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/temas/100"))
                .andExpect(status().isNoContent());

        verify(temaService).eliminarTemaPorId(100L);
    }

    @Test
    void eliminarTema_profesorSinPermisos_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("El usuario no tiene permiso para eliminar este tema."))
                .when(temaService).eliminarTemaPorId(100L);

        mockMvc.perform(delete("/api/temas/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarTema_temaNoExiste_devuelve422() throws Exception {
        doThrow(new IllegalArgumentException("Tema no encontrado"))
                .when(temaService).eliminarTemaPorId(999L);

        mockMvc.perform(delete("/api/temas/999"))
                .andExpect(status().isUnprocessableEntity());
    }

    // -------------------------------------------------------
    // PROFESOR - Visualizacion de actividades del tema
    // -------------------------------------------------------

    @Test
    void verTema_profesorPropietario_devuelveTemaConActividades() throws Exception {
        Actividad act1 = fixtureActividad(601L, "Actividad Maestro 1", 7, 1);
        Actividad act2 = fixtureActividad(602L, "Actividad Maestro 2", 5, 2);
        when(temaService.encontrarTemaPorId(100L)).thenReturn(temaFracciones);
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(act1, act2));

        mockMvc.perform(get("/api/temas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.titulo").value("Fracciones"))
                .andExpect(jsonPath("$.actividades.length()").value(2))
                .andExpect(jsonPath("$.actividades[0].id").value(601L))
                .andExpect(jsonPath("$.actividades[1].id").value(602L));
    }

    @Test
    void listarTemasMaestro_propietario_devuelve200ConActividades() throws Exception {
        Actividad actividad = fixtureActividad(601L, "Actividad Maestro", 7, 1);
        when(temaService.encontrarTemasPorCursoMaestroId(10L)).thenReturn(List.of(temaFracciones));
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(actividad));

        mockMvc.perform(get("/api/temas/curso/10/maestro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].titulo").value("Fracciones"))
                .andExpect(jsonPath("$[0].actividades.length()").value(1))
                .andExpect(jsonPath("$[0].actividades[0].id").value(601L));

        verify(temaService).encontrarTemasPorCursoMaestroId(10L);
        verify(actividadService).encontrarActividadesPorTema(100L);
    }

    @Test
    void listarTemasMaestro_noPropietario_devuelve403() throws Exception {
        when(temaService.encontrarTemasPorCursoMaestroId(10L))
                .thenThrow(new AccessDeniedException("El maestro no es propietario del curso."));

        mockMvc.perform(get("/api/temas/curso/10/maestro"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // ALUMNO - Visualizacion de actividades del tema
    // -------------------------------------------------------

    @Test
    void verTema_alumnoInscrito_devuelveTemaConActividades() throws Exception {
        Actividad actividad = fixtureActividad(501L, "Actividad 1", 8, 1);
        when(temaService.encontrarTemaPorId(100L)).thenReturn(temaFracciones);
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(actividad));

        mockMvc.perform(get("/api/temas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.titulo").value("Fracciones"))
                .andExpect(jsonPath("$.cursoId").value(10L))
                .andExpect(jsonPath("$.actividades.length()").value(1))
                .andExpect(jsonPath("$.actividades[0].id").value(501L))
                .andExpect(jsonPath("$.actividades[0].titulo").value("Actividad 1"))
                .andExpect(jsonPath("$.actividades[0].puntuacion").value(8));
    }

    @Test
    void listarTemasAlumno_inscrito_devuelve200ConActividades() throws Exception {
        Actividad actividadTema1 = fixtureActividad(501L, "Actividad 1", 5, 1);
        Actividad actividadTema2 = fixtureActividad(502L, "Actividad 2", 10, 2);

        when(temaService.encontrarTemasPorCursoAlumnoId(10L)).thenReturn(List.of(temaFracciones, temaGeometria));
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(actividadTema1));
        when(actividadService.encontrarActividadesPorTema(101L)).thenReturn(List.of(actividadTema2));

        mockMvc.perform(get("/api/temas/curso/10/alumno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].titulo").value("Fracciones"))
                .andExpect(jsonPath("$[0].actividades.length()").value(1))
                .andExpect(jsonPath("$[1].id").value(101L))
                .andExpect(jsonPath("$[1].titulo").value("Geometria"))
                .andExpect(jsonPath("$[1].actividades.length()").value(1));

        verify(temaService).encontrarTemasPorCursoAlumnoId(10L);
        verify(actividadService).encontrarActividadesPorTema(100L);
        verify(actividadService).encontrarActividadesPorTema(101L);
    }

    // -------------------------------------------------------
    // ALUMNO - Acceso sin autorizacion
    // -------------------------------------------------------

    @Test
    void verTema_alumnoNoInscrito_devuelve403() throws Exception {
        when(temaService.encontrarTemaPorId(100L))
                .thenThrow(new AccessDeniedException("Solo alguien perteneciente al curso puede acceder a este tema"));

        mockMvc.perform(get("/api/temas/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarTemasAlumno_noInscrito_devuelve403() throws Exception {
        when(temaService.encontrarTemasPorCursoAlumnoId(10L))
                .thenThrow(new AccessDeniedException("El alumno logueado no esta inscrito en este curso."));

        mockMvc.perform(get("/api/temas/curso/10/alumno"))
                .andExpect(status().isForbidden());

        verify(actividadService, never()).encontrarActividadesPorTema(any());
    }

    @Test
    void verTema_temaNoExiste_devuelve422() throws Exception {
        when(temaService.encontrarTemaPorId(999L))
                .thenThrow(new IllegalArgumentException("Tema no encontrado con ID: 999"));

        mockMvc.perform(get("/api/temas/999"))
                .andExpect(status().isUnprocessableEntity());
    }

    // -------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------

    private Curso fixtureCurso(Long cursoId, Long maestroId) {
        Maestro maestro = new Maestro();
        maestro.setId(maestroId);

        Curso curso = new Curso();
        curso.setId(cursoId);
        curso.setTitulo("Matematicas");
        curso.setMaestro(maestro);
        return curso;
    }

    private Actividad fixtureActividad(Long id, String titulo, Integer puntuacion, Integer posicion) {
        Actividad actividad = new Actividad() { };
        actividad.setId(id);
        actividad.setTitulo(titulo);
        actividad.setDescripcion("Descripcion " + titulo);
        actividad.setPuntuacion(puntuacion);
        actividad.setPosicion(posicion);
        return actividad;
    }

    private record CrearTemaPayload(String titulo, Long cursoId) {}
    private record RenombrarTemaPayload(String nuevoTitulo) {}
}