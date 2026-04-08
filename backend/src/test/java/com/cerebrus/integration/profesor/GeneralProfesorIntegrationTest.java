package com.cerebrus.integration.profesor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.cerebrus.actividad.general.General;
import com.cerebrus.actividad.general.GeneralController;
import com.cerebrus.actividad.general.GeneralService;
import com.cerebrus.actividad.general.dto.CrucigramaDTO;
import com.cerebrus.actividad.general.dto.GeneralAbiertaMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralCartaDTO;
import com.cerebrus.actividad.general.dto.GeneralClasificacionMaestroDTO;
import com.cerebrus.actividad.general.dto.GeneralTestDTO;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GeneralProfesorIntegrationTest {

    @Mock
    private GeneralService generalService;

    @InjectMocks
    private GeneralController generalController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(generalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearTipoTest_ok_devuelve201ConId() throws Exception {
        General creada = new General();
        creada.setId(101L);

        when(generalService.crearActTipoTest(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(creada);

        Map<String, Object> body = Map.of(
                "titulo", "Test de Historia",
                "descripcion", "descripcion",
                "puntuacion", 10,
                "respVisible", true,
                "comentariosRespVisible", "comentario",
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 11L), Map.of("id", 12L))
        );

        mockMvc.perform(post("/api/generales/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(101));
    }

    @Test
    void crearTipoCarta_ok_devuelve201ConId() throws Exception {
        General creada = new General();
        creada.setId(102L);
        when(generalService.crearActCarta(any(), any(), any(), any(), any(), any(), any())).thenReturn(creada);

        Map<String, Object> body = Map.of(
                "titulo", "Carta de Geografia",
                "descripcion", "descripcion",
                "puntuacion", 10,
                "respVisible", true,
                "comentariosRespVisible", "comentario",
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 21L), Map.of("id", 22L))
        );

        mockMvc.perform(post("/api/generales/cartas/maestro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(102));
    }

    @Test
    void crearTipoClasificacion_ok_devuelve201ConId() throws Exception {
        General creada = new General();
        creada.setId(103L);
        when(generalService.crearActClasificacion(any(), any(), any(), any(), any(), any())).thenReturn(creada);

        Map<String, Object> body = Map.of(
                "titulo", "Clasificacion",
                "descripcion", "descripcion",
                "puntuacion", 10,
                "respVisible", true,
                "comentariosRespVisible", "comentario",
                "tema", Map.of("id", 1L)
        );

        mockMvc.perform(post("/api/generales/clasificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(103));
    }

    @Test
    void crearTipoAbierta_ok_devuelve201ConId() throws Exception {
        General creada = new General();
        creada.setId(104L);
        when(generalService.crearActAbierta(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(creada);

        Map<String, Object> body = Map.of(
                "titulo", "Abierta",
                "descripcion", "descripcion",
                "puntuacion", 10,
                "respVisible", true,
                "comentariosRespVisible", "comentario",
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 31L)),
                "imagen", "img.png"
        );

        mockMvc.perform(post("/api/generales/abierta/maestro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(104));
    }

    @Test
    void crearCrucigrama_respuestaInvalida_devuelve400() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "Crucigrama 1",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "preguntasYRespuestas", Map.of("Capital de Francia", "Par1s")
        );

        mockMvc.perform(post("/api/generales/crucigrama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCrucigrama_masDeCincoPreguntas_devuelve400() throws Exception {
        Map<String, String> preguntas = Map.of(
                "p1", "uno",
                "p2", "dos",
                "p3", "tres",
                "p4", "cuatro",
                "p5", "cinco",
                "p6", "seis"
        );

        Map<String, Object> body = Map.of(
                "titulo", "Crucigrama 2",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "preguntasYRespuestas", preguntas
        );

        mockMvc.perform(post("/api/generales/crucigrama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCrucigrama_ok_devuelve200ConPayload() throws Exception {
        CrucigramaDTO dto = org.mockito.Mockito.mock(CrucigramaDTO.class);
        when(dto.getId()).thenReturn(88L);
        when(generalService.crearActCrucigrama(any())).thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Crucigrama 3",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "preguntasYRespuestas", Map.of("Capital", "Paris")
        );

        mockMvc.perform(post("/api/generales/crucigrama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void crearGeneral_payloadInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "",
                "descripcion", "desc",
                "puntuacion", 10,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "tema", Map.of("id", 1L)
        );

        mockMvc.perform(post("/api/generales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void actualizarGeneral_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("Solo el maestro del curso puede actualizar esta actividad"))
                .when(generalService)
                .actualizarActGeneral(eq(77L), any(), any(), any(), any(), any(), any(), any(), any(), any());

        Map<String, Object> body = Map.of(
                "titulo", "Edit",
                "descripcion", "desc",
                "puntuacion", 9,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "posicion", 1,
                "version", 1,
                "tema", Map.of("id", 1L)
        );

        mockMvc.perform(put("/api/generales/update/77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el maestro del curso puede actualizar esta actividad"));
    }

    @Test
    void actualizarTipoTest_ok_devuelve200() throws Exception {
        GeneralTestDTO dto = org.mockito.Mockito.mock(GeneralTestDTO.class);
        when(generalService.encontrarActTipoTestPorId(12L)).thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Edit test",
                "descripcion", "desc",
                "puntuacion", 9,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "posicion", 1,
                "version", 1,
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 11L))
        );

        mockMvc.perform(put("/api/generales/test/update/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarTipoCarta_ok_devuelve200() throws Exception {
        GeneralCartaDTO dto = org.mockito.Mockito.mock(GeneralCartaDTO.class);
        when(generalService.encontrarActCartaPorId(13L)).thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Edit carta",
                "descripcion", "desc",
                "puntuacion", 9,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "posicion", 1,
                "version", 1,
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 21L))
        );

        mockMvc.perform(put("/api/generales/cartas/update/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarTipoClasificacion_ok_devuelve200() throws Exception {
        GeneralClasificacionMaestroDTO dto = org.mockito.Mockito.mock(GeneralClasificacionMaestroDTO.class);
        when(generalService.actualizarActClasificacion(eq(14L), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Edit clasificacion",
                "descripcion", "desc",
                "puntuacion", 9,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "posicion", 1,
                "version", 1,
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 31L))
        );

        mockMvc.perform(put("/api/generales/clasificacion/update/14")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarTipoAbierta_ok_devuelve200() throws Exception {
        GeneralAbiertaMaestroDTO dto = org.mockito.Mockito.mock(GeneralAbiertaMaestroDTO.class);
        when(generalService.encontrarActAbiertaMaestroPorId(15L)).thenReturn(dto);

        Map<String, Object> body = Map.of(
                "titulo", "Edit abierta",
                "descripcion", "desc",
                "puntuacion", 9,
                "respVisible", true,
                "comentariosRespVisible", "ok",
                "posicion", 1,
                "version", 1,
                "tema", Map.of("id", 1L),
                "preguntas", List.of(Map.of("id", 41L)),
                "imagen", "img.png"
        );

        mockMvc.perform(put("/api/generales/abierta/update/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarCrucigrama_respuestaInvalida_devuelve400() throws Exception {
        Map<String, Object> body = Map.of(
                "titulo", "Crucigrama edit",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "preguntasYRespuestas", Map.of("Capital", "Par1s")
        );

        mockMvc.perform(put("/api/generales/crucigrama/16")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarCrucigrama_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No tienes permiso para actualizar este crucigrama"))
                .when(generalService).actualizarActCrucigrama(eq(16L), any());

        Map<String, Object> body = Map.of(
                "titulo", "Crucigrama edit",
                "descripcion", "desc",
                "puntuacion", 10,
                "temaId", 1,
                "respVisible", true,
                "preguntasYRespuestas", Map.of("Capital", "Paris")
        );

        mockMvc.perform(put("/api/generales/crucigrama/16")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para actualizar este crucigrama"));
    }

    @Test
    void eliminarGeneral_noPropietario_devuelve403() throws Exception {
        doThrow(new AccessDeniedException("No puedes eliminar actividades de cursos que no son tuyos"))
                .when(generalService).eliminarActGeneralPorId(55L);

        mockMvc.perform(delete("/api/generales/delete/55"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No puedes eliminar actividades de cursos que no son tuyos"));
    }

        @Test
        void eliminarGeneral_ok_devuelve204() throws Exception {
                mockMvc.perform(delete("/api/generales/delete/55"))
                                .andExpect(status().isNoContent());
        }
}
