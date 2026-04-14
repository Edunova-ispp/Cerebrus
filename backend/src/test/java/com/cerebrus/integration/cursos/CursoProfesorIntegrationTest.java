package com.cerebrus.integration.cursos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

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
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CursoProfesorIntegrationTest {

    @Mock
    private CursoServiceImpl cursoService;

    @InjectMocks
    private CursoController cursoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cursoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearCurso_ok_devuelve201() throws Exception {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setTitulo("Java Integration");

        when(cursoService.crearCurso(any(), any(), any(), any())).thenReturn(curso);

        Map<String, Object> body = Map.of(
                "titulo", "Java Integration",
                "descripcion", "Curso de tests",
                "imagen", "logo.png"
        );

        mockMvc.perform(post("/api/cursos/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarCurso_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo el propietario del curso puede actualizarlo"))
                .when(cursoService).actualizarCurso(eq(1L), any(), any(), any(), any());

        Map<String, Object> body = Map.of("titulo", "Nuevo Titulo");

        mockMvc.perform(patch("/api/cursos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarCurso_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/cursos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cambiarVisibilidad_ok_devuelve200() throws Exception {
        Curso curso = new Curso();
        curso.setVisibilidad(true);
        when(cursoService.cambiarVisibilidad(1L)).thenReturn(curso);

        mockMvc.perform(patch("/api/cursos/1/visibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibilidad").value(true));
    }
}
