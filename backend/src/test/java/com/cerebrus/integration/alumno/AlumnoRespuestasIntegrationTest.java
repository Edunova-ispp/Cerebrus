package com.cerebrus.integration.alumno;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.puntoImagen.PuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralController;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneralService;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaRequest;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.EvaluacionActividadAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoAbiertaResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralCreateResponse;
import com.cerebrus.respuestaAlumn.respAlumGeneral.dto.RespAlumnoGeneralRequest;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacionController;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.RespAlumnoOrdenacionService;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionCreateResponse;
import com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto.RespAlumnoOrdenacionDTO;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagen;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagenController;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.RespAlumnoPuntoImagenService;
import com.cerebrus.respuestaAlumn.respAlumPuntoImagen.dto.RespAlumnoPuntoImagenDTO;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AlumnoRespuestasIntegrationTest {

    @Mock
    private RespAlumnoGeneralService respAlumnoGeneralService;

    @Mock
    private RespAlumnoOrdenacionService respAlumnoOrdenacionService;

    @Mock
    private RespAlumnoPuntoImagenService respAlumnoPuntoImagenService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private RespAlumnoGeneralController respAlumnoGeneralController;

    @InjectMocks
    private RespAlumnoOrdenacionController respAlumnoOrdenacionController;

    @InjectMocks
    private RespAlumnoPuntoImagenController respAlumnoPuntoImagenController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        respAlumnoGeneralController,
                        respAlumnoOrdenacionController,
                        respAlumnoPuntoImagenController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearRespuestaAlumnoGeneral_ok_devuelve201() throws Exception {
        RespAlumnoGeneralCreateResponse created = new RespAlumnoGeneralCreateResponse(9001L, true, "Correcto");
        when(respAlumnoGeneralService.crearRespuestaAlumnoGeneral(8010L, 7103L, 7002L)).thenReturn(created);

        RespAlumnoGeneralRequest request = new RespAlumnoGeneralRequest();
        request.setActividadAlumnoId(8010L);
        request.setPreguntaId(7002L);
        request.setRespuestaId(7103L);

        mockMvc.perform(post("/api/respuestas-alumno-general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.correcta").value(true))
                .andExpect(jsonPath("$.comentario").value("Correcto"));
    }

    @Test
    void crearRespuestaAlumnoGeneral_payloadInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "actividadAlumnoId", 8010L,
                "preguntaId", 7002L
        );

        mockMvc.perform(post("/api/respuestas-alumno-general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void corregirCrucigrama_vacio_devuelve422() throws Exception {
        mockMvc.perform(post("/api/respuestas-alumno-general/crucigrama/7001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void corregirCrucigrama_masDeCincoPreguntas_devuelve422() throws Exception {
        LinkedHashMap<Long, String> body = new LinkedHashMap<>();
        body.put(1L, "uno");
        body.put(2L, "dos");
        body.put(3L, "tres");
        body.put(4L, "cuatro");
        body.put(5L, "cinco");
        body.put(6L, "seis");

        mockMvc.perform(post("/api/respuestas-alumno-general/crucigrama/7001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void corregirCrucigrama_ok_devuelve200() throws Exception {
        LinkedHashMap<Long, String> body = new LinkedHashMap<>();
        body.put(7001L, "HyperText Markup Language");

        LinkedHashMap<Long, String> resultado = new LinkedHashMap<>();
        resultado.put(7001L, "correcta");
        resultado.put(-1L, "10");

        when(respAlumnoGeneralService.corregirCrucigrama(any(), eq(7001L))).thenReturn(resultado);

        mockMvc.perform(post("/api/respuestas-alumno-general/crucigrama/7001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.7001").value("correcta"))
                .andExpect(jsonPath("$.-1").value("10"));
    }

    @Test
    void corregirActividadAbierta_ok_devuelve201() throws Exception {
        EvaluacionActividadAbiertaResponse response = new EvaluacionActividadAbiertaResponse(
                10,
                10,
                List.of(new RespAlumnoAbiertaResponse(9101L, 10, "Bien", true, "Excelente"))
        );
        when(respAlumnoGeneralService.corregirActividadAbierta(any())).thenReturn(response);

        EvaluacionActividadAbiertaRequest request = new EvaluacionActividadAbiertaRequest();
        request.setActividadAlumnoId(8010L);
        LinkedHashMap<Long, String> respuestas = new LinkedHashMap<>();
        respuestas.put(7002L, "Un molde para objetos");
        request.setRespuestasAlumno(respuestas);

        mockMvc.perform(post("/api/respuestas-alumno-general/abierta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notaFinal").value(10))
                .andExpect(jsonPath("$.puntuacionFinal").value(10));
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_ok_devuelve201() throws Exception {
        Alumno alumno = new Alumno();
        alumno.setId(2101L);
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        RespAlumnoOrdenacionDTO dto = new RespAlumnoOrdenacionDTO(9201L, true);
        RespAlumnoOrdenacionCreateResponse created = new RespAlumnoOrdenacionCreateResponse(dto, "Perfecto");
        when(respAlumnoOrdenacionService.crearRespuestaAlumnoOrdenacion(8002L, List.of("<html>", "<head>", "<body>"), 6002L))
                .thenReturn(created);

        Map<String, Object> body = Map.of(
                "actividadAlumno", Map.of("id", 8002L),
                "ordenacion", Map.of("id", 6002L),
                "valoresAlum", List.of("<html>", "<head>", "<body>")
        );

        mockMvc.perform(post("/api/respuestas-alumno-ordenacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.respAlumnoOrdenacion.id").value(9201))
                .andExpect(jsonPath("$.respAlumnoOrdenacion.correcta").value(true))
                .andExpect(jsonPath("$.comentario").value("Perfecto"));
    }

    @Test
    void crearRespuestaAlumnoOrdenacion_usuarioNoAlumno_devuelve403() throws Exception {
        Maestro maestro = new Maestro();
        maestro.setId(2001L);
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> body = Map.of(
                "actividadAlumno", Map.of("id", 8002L),
                "ordenacion", Map.of("id", 6002L),
                "valoresAlum", List.of("<html>", "<head>", "<body>")
        );

        mockMvc.perform(post("/api/respuestas-alumno-ordenacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo un alumno puede crear respuestas de alumno"));
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_ok_devuelve201() throws Exception {
        RespAlumnoPuntoImagen entidad = new RespAlumnoPuntoImagen();
        entidad.setId(9301L);
        entidad.setRespuesta("HTML");
        PuntoImagen puntoImagen = new PuntoImagen();
        puntoImagen.setId(901L);
        entidad.setPuntoImagen(puntoImagen);
        ActividadAlumno actividadAlumno = new ActividadAlumno();
        actividadAlumno.setId(8008L);
        entidad.setActividadAlumno(actividadAlumno);

        when(respAlumnoPuntoImagenService.crearRespuestaAlumnoPuntoImagen("HTML", 901L, 8008L)).thenReturn(entidad);

        RespAlumnoPuntoImagenDTO request = new RespAlumnoPuntoImagenDTO(null, "HTML", 901L, 8008L);

        mockMvc.perform(post("/api/respuestas-alumno-punto-imagen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9301))
                .andExpect(jsonPath("$.respuesta").value("HTML"))
                .andExpect(jsonPath("$.puntoImagenId").value(901))
                .andExpect(jsonPath("$.actividadAlumnoId").value(8008));
    }

    @Test
    void crearRespuestaAlumnoPuntoImagen_payloadInvalido_devuelve422() throws Exception {
        Map<String, Object> body = Map.of(
                "respuesta", "HTML",
                "actividadAlumnoId", 8008L
        );

        mockMvc.perform(post("/api/respuestas-alumno-punto-imagen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableContent());
    }
}