package com.cerebrus.integration.cursos;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.curso.CursoController;
import com.cerebrus.curso.CursoServiceImpl;
import com.cerebrus.exceptions.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class CursoAlumnoIntegrationTest {

    @Mock
    private CursoServiceImpl cursoService;

    @InjectMocks
    private CursoController cursoController;

    private MockMvc mockMvc;

    @BeforeEach
void setUp() {
    mockMvc = MockMvcBuilders
            .standaloneSetup(cursoController)
            .setControllerAdvice(new GlobalExceptionHandler()) 
            .build();
}

    @Test
    void verDetallesCurso_inscrito_ok() throws Exception {
        List<String> detalles = List.of("Titulo", "Descripcion", "Imagen");
        when(cursoService.encontrarDetallesCursoPorId(1L)).thenReturn(detalles);

        mockMvc.perform(get("/api/cursos/1/detalles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Titulo"));
    }

    @Test
    void verDetallesCurso_noInscrito_devuelve403() throws Exception {
        // Simulamos que el service lanza el error de negocio definido
        when(cursoService.encontrarDetallesCursoPorId(1L))
                .thenThrow(new RuntimeException("403 Forbidden"));

        mockMvc.perform(get("/api/cursos/1/detalles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void verMisCursos_sinSerAlumnoOMaestro_devuelve403() throws Exception {
       
        when(cursoService.encontrarCursosPorUsuarioLogueado())
                .thenThrow(new org.springframework.security.access.AccessDeniedException("403 Forbidden"));

        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isForbidden());
    }

    @Test
    void verProgreso_cursoNoExistente_devuelve404() throws Exception {
        when(cursoService.encontrarProgresoPorCursoId(99L))
                .thenThrow(new RuntimeException("404 Not Found"));

        mockMvc.perform(get("/api/cursos/99/progreso"))
                .andExpect(status().isNotFound());
    }
}
