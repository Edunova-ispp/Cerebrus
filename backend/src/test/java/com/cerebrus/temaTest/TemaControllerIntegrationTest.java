package com.cerebrus.temaTest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.cerebrus.actividad.Actividad;
import com.cerebrus.actividad.ActividadService;
import com.cerebrus.curso.Curso;
import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaService;
import com.cerebrus.usuario.maestro.Maestro;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
    "GOOGLE_API_KEY_1=dummy-key-1",
    "GOOGLE_API_KEY_2=dummy-key-2",
    "GOOGLE_API_KEY_3=dummy-key-3",
    "GOOGLE_API_KEY_4=dummy-key-4",
    "GOOGLE_API_KEY_5=dummy-key-5"
})
class TemaControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TemaService temaService;

    @MockitoBean
    private ActividadService actividadService;

    private Tema temaFracciones;
    private Tema temaGeometria;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Curso curso = fixtureCurso(10L, 1L);
        temaFracciones = new Tema("Fracciones", curso);
        temaFracciones.setId(100L);

        temaGeometria = new Tema("Geometria", curso);
        temaGeometria.setId(101L);
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion crear tema se devuelve 201.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void crearTema_maestroValido_devuelve201YPayloadEsperado() throws Exception {
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

    // Test para comprobar que con el usuario ALUMNO al ejecutar la accion crear tema se devuelve 403.
    @Test
    @WithMockUser(authorities = "ALUMNO")
    void crearTema_usuarioNoMaestro_devuelve403() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(temaService, never()).crearTema(any(), any(), any());
    }

    // Test para comprobar que con el usuario SIN_AUTENTICAR al ejecutar la accion crear tema se devuelve 403.
    @Test
    void crearTema_usuarioNoAutenticado_devuelve403() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion crear tema con payload invalido se devuelve 422.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void crearTema_payloadInvalido_devuelve422() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload(" ", null));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        verify(temaService, never()).crearTema(any(), any(), any());
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion crear tema sin ser propietario se devuelve 400.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void crearTema_maestroNoPropietario_devuelve400() throws Exception {
        when(temaService.crearTema("Fracciones", 10L, 2L))
                .thenThrow(new IllegalArgumentException("El maestro no es propietario del curso"));

        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                        .param("maestroId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion renombrar tema valido se devuelve 200.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void renombrarTema_maestroValido_devuelve200YTemaActualizado() throws Exception {
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
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion renombrar tema con payload invalido se devuelve 422.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void renombrarTema_payloadInvalido_devuelve422YSinPersistirCambios() throws Exception {
        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload(""));

        mockMvc.perform(put("/api/temas/100")
                        .param("maestroId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        verify(temaService, never()).renombrarTema(any(), any(), any());
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion eliminar tema propio se devuelve 204.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void eliminarTema_maestroPropietario_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/temas/100"))
                .andExpect(status().isNoContent());

        verify(temaService).eliminarTemaPorId(100L);
    }

    // Test para comprobar que con el usuario MAESTRO al ejecutar la accion eliminar tema sin permisos se devuelve 403.
    @Test
    @WithMockUser(authorities = "MAESTRO")
    void eliminarTema_sinPermisosDevueltoPorServicio_devuelve403() throws Exception {
        when(temaService.encontrarTemaPorId(100L)).thenReturn(temaFracciones);
        org.mockito.Mockito.doThrow(new AccessDeniedException("El usuario no tiene permiso para eliminar este tema."))
                .when(temaService).eliminarTemaPorId(100L);

        mockMvc.perform(delete("/api/temas/100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Test para comprobar que con el usuario ALUMNO al ejecutar la accion consultar tema autorizado se devuelve 200.
    @Test
    @WithMockUser(authorities = "ALUMNO")
    void encontrarTemaPorId_alumnoAutorizado_devuelveTemaConActividades() throws Exception {
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

    // Test para comprobar que con el usuario ALUMNO al ejecutar la accion consultar tema sin acceso se devuelve 403.
    @Test
    @WithMockUser(authorities = "ALUMNO")
    void encontrarTemaPorId_alumnoSinAcceso_devuelve403() throws Exception {
        when(temaService.encontrarTemaPorId(100L))
                .thenThrow(new AccessDeniedException("Solo alguien perteneciente al curso puede acceder a este tema"));

        mockMvc.perform(get("/api/temas/100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Test para comprobar que con el usuario ALUMNO al ejecutar la accion listar temas de curso autorizado se devuelve 200.
    @Test
    @WithMockUser(authorities = "ALUMNO")
    void listarTemasAlumno_devuelveTodosLosTemasConDatosNecesariosParaMapa() throws Exception {
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
                .andExpect(jsonPath("$[0].cursoId").value(10L))
                .andExpect(jsonPath("$[0].actividades.length()").value(1))
                .andExpect(jsonPath("$[1].id").value(101L))
                .andExpect(jsonPath("$[1].titulo").value("Geometria"))
                .andExpect(jsonPath("$[1].cursoId").value(10L))
                .andExpect(jsonPath("$[1].actividades.length()").value(1));

        verify(temaService).encontrarTemasPorCursoAlumnoId(10L);
        verify(actividadService).encontrarActividadesPorTema(100L);
        verify(actividadService).encontrarActividadesPorTema(101L);
    }

    // Test para comprobar que con el usuario ALUMNO al ejecutar la accion listar temas de curso no autorizado se devuelve 403.
    @Test
    @WithMockUser(authorities = "ALUMNO")
    void listarTemasAlumno_sinAutorizacionParaCurso_devuelve403() throws Exception {
        when(temaService.encontrarTemasPorCursoAlumnoId(10L))
                .thenThrow(new AccessDeniedException("El alumno logueado no está inscrito en este curso."));

        mockMvc.perform(get("/api/temas/curso/10/alumno"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(actividadService, never()).encontrarActividadesPorTema(any());
    }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion listar temas maestro de curso propio se devuelve 200.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void listarTemasMaestro_maestroPropietario_devuelve200() throws Exception {
        Actividad actividad = fixtureActividad(601L, "Actividad Maestro", 7, 1);

        when(temaService.encontrarTemasPorCursoMaestroId(10L)).thenReturn(List.of(temaFracciones));
        when(actividadService.encontrarActividadesPorTema(100L)).thenReturn(List.of(actividad));

        mockMvc.perform(get("/api/temas/curso/10/maestro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(100L))
            .andExpect(jsonPath("$[0].titulo").value("Fracciones"))
            .andExpect(jsonPath("$[0].actividades.length()").value(1));
        }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion listar temas maestro de curso ajeno se devuelve 403.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void listarTemasMaestro_maestroNoPropietario_devuelve403() throws Exception {
        when(temaService.encontrarTemasPorCursoMaestroId(10L))
            .thenThrow(new AccessDeniedException("El maestro no es propietario del curso."));

        mockMvc.perform(get("/api/temas/curso/10/maestro"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
        }

        // Test para comprobar que con el usuario SIN_AUTENTICAR al ejecutar la accion listar temas maestro se devuelve 403.
        @Test
        void listarTemasMaestro_sinAutenticar_devuelve403() throws Exception {
        mockMvc.perform(get("/api/temas/curso/10/maestro"))
            .andExpect(status().isForbidden());
        }

        // Test para comprobar que con el usuario ALUMNO al ejecutar la accion consultar tema inexistente se devuelve 422.
        @Test
        @WithMockUser(authorities = "ALUMNO")
        void encontrarTemaPorId_noExiste_devuelve422() throws Exception {
        when(temaService.encontrarTemaPorId(999L))
            .thenThrow(new IllegalArgumentException("Tema no encontrado con ID: 999"));

        mockMvc.perform(get("/api/temas/999"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422));
        }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion renombrar tema inexistente se devuelve 400.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void renombrarTema_temaNoExiste_devuelve400() throws Exception {
        when(temaService.renombrarTema(999L, "Nuevo", 1L))
            .thenThrow(new IllegalArgumentException("Tema no encontrado"));

        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Nuevo"));

        mockMvc.perform(put("/api/temas/999")
                .param("maestroId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
        }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion eliminar tema inexistente se devuelve 422.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void eliminarTema_temaNoExiste_devuelve422() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Tema no encontrado"))
            .when(temaService).eliminarTemaPorId(999L);

        mockMvc.perform(delete("/api/temas/999"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422));
        }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion crear tema sin maestroId se devuelve 400.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void crearTema_sinMaestroId_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(new CrearTemaPayload("Fracciones", 10L));

        mockMvc.perform(post("/api/temas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(temaService, never()).crearTema(any(), any(), any());
        }

        // Test para comprobar que con el usuario MAESTRO al ejecutar la accion renombrar tema con maestroId invalido se devuelve 400.
        @Test
        @WithMockUser(authorities = "MAESTRO")
        void renombrarTema_maestroIdInvalido_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(new RenombrarTemaPayload("Nuevo titulo"));

        mockMvc.perform(put("/api/temas/100")
                .param("maestroId", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(temaService, never()).renombrarTema(any(), any(), any());
        }

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

    private record CrearTemaPayload(String titulo, Long cursoId) {
    }

    private record RenombrarTemaPayload(String nuevoTitulo) {
    }
}
