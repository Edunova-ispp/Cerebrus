package com.cerebrus.integration.cursos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.cerebrus.curso.Curso;
import com.cerebrus.curso.CursoController;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.usuario.maestro.Maestro;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CreacionCursosIntegrationTest {

    @Mock
    private CursoServiceImpl cursoService;

    @InjectMocks
    private CursoController cursoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Maestro maestro;
    private Curso curso;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cursoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        maestro = new Maestro();
        maestro.setId(1L);
        maestro.setNombre("Profesor Test");

        curso = new Curso("Curso Test", "Descripción", "imagen.png", true, maestro);
        curso.setId(1L);
    }

    @Test
    void crearCurso_profesorAutorizado_retorna201() throws Exception {
        when(cursoService.crearCurso(eq("Nuevo Curso"), eq("Descripción"), eq("imagen.png")))
                .thenReturn(curso);

        mockMvc.perform(post("/api/cursos/curso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "titulo", "Nuevo Curso",
                        "descripcion", "Descripción",
                        "imagen", "imagen.png"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Curso Test"));
    }

    @Test
    void crearCurso_accesoDenegado_retorna403() throws Exception {
        doThrow(new AccessDeniedException("Access denied")).when(cursoService)
                .crearCurso(any(), any(), any());

        mockMvc.perform(post("/api/cursos/curso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "titulo", "Nuevo Curso",
                        "descripcion", "Descripción",
                        "imagen", "imagen.png"
                ))))
                .andExpect(status().isForbidden());
    }
}