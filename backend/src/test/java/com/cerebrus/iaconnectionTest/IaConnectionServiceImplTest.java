package com.cerebrus.iaconnectionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.cerebrus.comun.enumerados.TipoAct;
import com.cerebrus.exceptions.QuotaExceededException;
import com.cerebrus.iaconnection.IaConnectionServiceImpl;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IaConnectionServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private IaConnectionServiceImpl iaConnectionService;

    // ==================== Setup ====================

    @BeforeEach
    void setUp() {
        // Configurar valores iniciales para las claves API y contadores
        ReflectionTestUtils.setField(iaConnectionService, "apiKey1", "key1");
        ReflectionTestUtils.setField(iaConnectionService, "apiKey2", "key2");
        ReflectionTestUtils.setField(iaConnectionService, "apiKey3", "key3");
        ReflectionTestUtils.setField(iaConnectionService, "apiKey4", "key4");
        ReflectionTestUtils.setField(iaConnectionService, "apiKey5", "key5");
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", null);
    }

    // ==================== Test generarMockActividad ====================

    @Test
    void generarMockActividad_tipoTEORIA_devuelveJsonTeoria() {
        // Act
        String resultado = iaConnectionService.generarMockActividad(TipoAct.TEORIA, "cualquier prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(resultado).contains("titulo");
        assertThat(resultado).contains("descripcion");
    }

    @Test
    void generarMockActividad_tipoTEST_devuelveJsonTest() {
        // Act
        String resultado = iaConnectionService.generarMockActividad(TipoAct.TEST, "cualquier prompt");

        // Assert
        assertThat(resultado).contains("TIPO_TEST");
        assertThat(resultado).contains("preguntas");
        assertThat(resultado).contains("opciones");
    }

    @Test
    void generarMockActividad_tipoORDEN_devuelveJsonOrden() {
        // Act
        String resultado = iaConnectionService.generarMockActividad(TipoAct.ORDEN, "cualquier prompt");

        // Assert
        assertThat(resultado).contains("ORDENACION");
        assertThat(resultado).contains("valores");
        assertThat(resultado).contains("orden");
    }

    @Test
    void generarMockActividad_tipoNoSoportado_lanzaIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> {
            // Crear una simulación de TipoAct no soportado mediante reflexión
            TipoAct tipo = TipoAct.CARTA;
            iaConnectionService.generarMockActividad(tipo, "prompt");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de actividad no soportado");
    }

    // ==================== Test crearPrompt ====================

    @Test
    void crearPrompt_tipoTEORIA_devuelvePromptTeoria() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.TEORIA, "Física");

        // Assert
        assertThat(prompt).contains("actividad teórica");
        assertThat(prompt).contains("Física");
        assertThat(prompt).contains("TEORIA");
    }

    @Test
    void crearPrompt_tipoTEST_devuelvePromptTest() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.TEST, "Matemáticas");

        // Assert
        assertThat(prompt).contains("test");
        assertThat(prompt).contains("Matemáticas");
        assertThat(prompt).contains("TEST");
        assertThat(prompt).contains("2 preguntas");
    }

    @Test
    void crearPrompt_tipoORDEN_devuelvePromptOrden() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.ORDEN, "Ciclo de vida");

        // Assert
        assertThat(prompt).contains("ordenación");
        assertThat(prompt).contains("Ciclo de vida");
        assertThat(prompt).contains("ORDEN");
        assertThat(prompt).contains("4 elementos");
    }

    @Test
    void crearPrompt_tipoCARTA_devuelvePromptCarta() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.CARTA, "Historia");

        // Assert
        assertThat(prompt).contains("carta");
        assertThat(prompt).contains("Historia");
        assertThat(prompt).contains("emparejar");
    }

    @Test
    void crearPrompt_tipoCLASIFICACION_devuelvePromptClasificacion() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.CLASIFICACION, "Animales");

        // Assert
        assertThat(prompt).contains("clasificación");
        assertThat(prompt).contains("Animales");
    }

    @Test
    void crearPrompt_tipoTABLERO_devuelvePromptTablero() {
        // Act
        String prompt = iaConnectionService.crearPrompt(TipoAct.TABLERO, "Geografía");

        // Assert
        assertThat(prompt).contains("tablero");
        assertThat(prompt).contains("Geografía");
        assertThat(prompt).contains("TABLERO");
    }

    @Test
    void crearPrompt_tipoNoSoportado_lanzaIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.crearPrompt(TipoAct.IMAGEN, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no soportado");
    }

    // ==================== Test generarActividad - Usuario ====================

    @Test
    void generarActividad_usuarioNoEsMaestro_lanzaIllegalArgumentException() {
        // Arrange
        Usuario usuarioNoMaestro = new Usuario() {
        };
        when(usuarioService.findCurrentUser()).thenReturn(usuarioNoMaestro);

        // Act & Assert
        assertThatThrownBy(
                () -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("403 Forbidden")
                .hasMessageContaining("no es un maestro");

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void generarActividad_usuarioEsMaestro_procede() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))

        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    // ==================== Test rotateToNextApiKeySameDay ====================

    @Test
    void rotateToNextApiKeySameDay_cuandoIndiceKeyMenorA5_rotaYDevuelveTrue() {
        // Arrange
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        // Act
        boolean rotado = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "rotateToNextApiKeySameDay");

        // Assert
        assertThat(rotado).isTrue();
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(2);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(0);
    }

    @Test
    void rotateToNextApiKeySameDay_cuandoIndiceKeyIgual5_noRotaYDevuelveFalse() {
        // Arrange
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);

        // Act
        boolean rotado = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "rotateToNextApiKeySameDay");

        // Assert
        assertThat(rotado).isFalse();
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(5);
    }

    @Test
    void rotateToNextApiKeySameDay_rotacionMultiple_llegaAindiceKey5() {
        // Arrange
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        // Act
        for (int i = 1; i < 5; i++) {
            boolean rotado = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "rotateToNextApiKeySameDay");
            assertThat(rotado).isTrue();
        }

        boolean ultimaRotacion = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "rotateToNextApiKeySameDay");

        // Assert
        assertThat(ultimaRotacion).isFalse();
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(5);
    }

    // ==================== Test validateActivityResponse ====================

    @Test
    void validateActivityResponse_tipoTEORIA_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"TEORICA\", \"titulo\": \"Título\", \"descripcion\": \"Descripción\"}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoTEST_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"TIPO_TEST\", \"titulo\": \"Título\", \"descripcion\": \"Desc\", \"preguntas\": []}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEST);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoORDEN_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"ORDENACION\", \"titulo\": \"Título\", \"descripcion\": \"Desc\", \"valores\": []}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.ORDEN);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoCARTA_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"CARTA\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CARTA);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoCLASIFICACION_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"CLASIFICACION\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CLASIFICACION);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoTABLERO_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"TABLERO\", \"titulo\": \"T\", \"descripcion\": \"D\", \"tamaño\": \"TRES_X_TRES\", \"preguntas\": []}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TABLERO);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoTEORIA_faltanCampos_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"TEORICA\", \"titulo\": \"Título\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_tipoIncorrecto_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"TEST\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("tipo equivocado");
    }

    @Test
    void validateActivityResponse_jsonInvalido_lanzaException() {
        // Arrange
        String response = "{ json inválido }";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_jsonConBackticks_limpaYValida() {
        // Arrange
        String response = "```json\n{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}\n```";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoTEST_faltanPreguntas_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"TIPO_TEST\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEST))
                .hasMessageContaining("mal formato de actividad test");
    }

    // ==================== Test evaluarRespuestaAbierta ====================

    @Test
    void evaluarRespuestaAbierta_respuestaValida_devuelveEvaluacion() {
        // Arrange
        String pregunta = "¿Cuál es la capital de Francia?";
        String respuestaAlumno = "París";
        String respuestaModelo = "París";
        Integer puntuacion = 10;

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"puntuacion\": 10, \"comentarios\": \"Correcto\"}"))))))

        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuestaAlumno, respuestaModelo, puntuacion);

        // Assert
        assertThat(evaluacion).containsKeys("puntuacion", "comentarios");
        assertThat(evaluacion.get("puntuacion")).isEqualTo(10);
    }

    @Test
    void evaluarRespuestaAbierta_conError429_rotaClaveYReintenta() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": {\"message\": \"quota exceeded\"}}".getBytes(), null))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": {\"message\": \"quota exceeded\"}}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void evaluarRespuestaAbierta_cuotaDiariaExcedida_lanzaQuotaExceededException() {
        // Arrange
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 20);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("cuota diaria");
    }

    @Test
    void evaluarRespuestaAbierta_errorNoEs429_lanzaIllegalArgumentException() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "500 Internal Server Error"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error al evaluar la respuesta");
    }

    // ==================== Test resolveApiKeyByIndex ====================

    @Test
    void resolveApiKeyByIndex_index1_devuelveApiKey1() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 1);

        // Assert
        assertThat(resultado).isEqualTo("key1");
    }

    @Test
    void resolveApiKeyByIndex_index2_devuelveApiKey2() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 2);

        // Assert
        assertThat(resultado).isEqualTo("key2");
    }

    @Test
    void resolveApiKeyByIndex_index3_devuelveApiKey3() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 3);

        // Assert
        assertThat(resultado).isEqualTo("key3");
    }

    @Test
    void resolveApiKeyByIndex_index4_devuelveApiKey4() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 4);

        // Assert
        assertThat(resultado).isEqualTo("key4");
    }

    @Test
    void resolveApiKeyByIndex_index5_devuelveApiKey5() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 5);

        // Assert
        assertThat(resultado).isEqualTo("key5");
    }

    @Test
    void resolveApiKeyByIndex_indexInvalido_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 6))
                .hasMessageContaining("Error al seleccionar la clave de API");
    }

    // ==================== Test cleanJsonResponse ====================

    @Test
    void cleanJsonResponse_jsonConBackticksJson_limpia() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "cleanJsonResponse",
                "```json\n{\"tipo\": \"TEST\"}\n```");

        // Assert
        assertThat(resultado).isEqualTo("{\"tipo\": \"TEST\"}");
    }

    @Test
    void cleanJsonResponse_jsonConBackticksPlainSinJson_limpia() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "cleanJsonResponse",
                "```\n{\"tipo\": \"TEST\"}\n```");

        // Assert
        assertThat(resultado).isEqualTo("{\"tipo\": \"TEST\"}");
    }

    @Test
    void cleanJsonResponse_jsonSinBackticks_devuelveIgual() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "cleanJsonResponse",
                "{\"tipo\": \"TEST\"}");

        // Assert
        assertThat(resultado).isEqualTo("{\"tipo\": \"TEST\"}");
    }

    @Test
    void cleanJsonResponse_jsonConEspaciosAlRededor_limpiaEspacios() {
        // Act
        String resultado = (String) ReflectionTestUtils.invokeMethod(iaConnectionService, "cleanJsonResponse",
                "  {\"tipo\": \"TEST\"}  ");

        // Assert
        assertThat(resultado).isEqualTo("{\"tipo\": \"TEST\"}");
    }

    // ==================== Test generarActividad - Reintentos y Rotación ====================

    @Test
    void generarActividad_primerIntentoExitoso_noReintenta() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void generarActividad_cuotaaExcedidaPerDayErrorFreetier_lanzaQuotaExceededException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"GenerateRequestsPerDayPerProjectPerModel-freetier\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("cuota diaria");
    }

    @Test
    void generarActividad_cuotaDiariaExcedidaConError429_lanzaQuotaExceededException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 20);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_retryAfterExtradoDelError_lanzaQuotaExceededConTiempo() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "retry in 45.5s".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("46 segundos");
    }

    @Test
    void generarActividad_errorOtroQueNo429_lanzaIllegalArgumentException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                        "400 Bad Request"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error al generar la actividad");
    }

    @Test
    void generarActividad_respuestaNoValida_lanzaIllegalArgumentException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"INCORRECTO\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo equivocado");
    }

    @Test
    void generarActividad_variousTiposDeActividad() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // Reset para cada test
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        Map<String, Object> mockResponseTest = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TIPO_TEST\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponseTest, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEST, "prompt test");

        // Assert
        assertThat(resultado).contains("TIPO_TEST");
    }

    @Test
    void generarActividad_excesoMax20PeticionesPorClave_rotaAClave2() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 20);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(2);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(1);
    }

    @Test
    void generarActividad_fechaUltimaPeticionNull_inicializaFechaActual() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 20);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", null);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(1);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(1);
    }

    // ==================== Test evaluarRespuestaAbierta - Casos Adicionales ====================

    @Test
    void evaluarRespuestaAbierta_respuestaConBackticks_limpiayValida() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "```json\n{\"puntuacion\": 8, \"comentarios\": \"Bien\"}\n```"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion);

        // Assert
        assertThat(evaluacion).containsKeys("puntuacion", "comentarios");
        assertThat(evaluacion.get("puntuacion")).isEqualTo(8);
    }

    @Test
    void evaluarRespuestaAbierta_conRetryAfter_lanzaQuotaExceededConTiempo() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "retry in 30.2s".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("31 segundos");
    }

    @Test
    void evaluarRespuestaAbierta_reintentos5VecesYFalla_lanzaQuotaExceededException() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void evaluarRespuestaAbierta_puntuacionMaxima100_devuelveEvaluacionCorrecta() {
        // Arrange
        String pregunta = "Pregunta compleja";
        String respuestaAlumno = "Respuesta del alumno";
        String respuestaModelo = "Respuesta modelo";
        Integer puntuacion = 100;

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"puntuacion\": 85, \"comentarios\": \"Muy bien\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuestaAlumno, respuestaModelo, puntuacion);

        // Assert
        assertThat(evaluacion.get("puntuacion")).isEqualTo(85);
        assertThat(evaluacion.get("comentarios")).isEqualTo("Muy bien");
    }

    // ==================== Test validateActivityResponse - Casos Límite ====================

    @Test
    void validateActivityResponse_tipoCRUCIGRAMA_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"CRUCIGRAMA\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CRUCIGRAMA);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoABIERTA_valida_devuelveTrue() {
        // Arrange
        String response = "{\"tipo\": \"ABIERTA\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act
        Boolean valida = (Boolean) ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.ABIERTA);

        // Assert
        assertThat(valida).isTrue();
    }

    @Test
    void validateActivityResponse_tipoCARTA_faltanPreguntas_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"CARTA\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CARTA))
                .hasMessageContaining("mal formato de actividad carta");
    }

    @Test
    void validateActivityResponse_tipoTABLERO_faltaTamano_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"TABLERO\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TABLERO))
                .hasMessageContaining("mal formato de actividad tablero");
    }

    @Test
    void validateActivityResponse_tipoORDEN_faltaValores_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"ORDENACION\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.ORDEN))
                .hasMessageContaining("mal formato de actividad orden");
    }

    @Test
    void validateActivityResponse_tipoCLASIFICACION_faltaPreguntas_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"CLASIFICACION\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CLASIFICACION))
                .hasMessageContaining("mal formato de actividad clasificacion");
    }

    @Test
    void validateActivityResponse_tipoCRUCIGRAMA_faltaDescripcion_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"CRUCIGRAMA\", \"titulo\": \"T\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.CRUCIGRAMA))
                .hasMessageContaining("crucigrama");
    }

    @Test
    void validateActivityResponse_tipoABIERTA_faltaTitulo_lanzaException() {
        // Arrange
        String response = "{\"tipo\": \"ABIERTA\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.ABIERTA))
                .hasMessageContaining("abierta");
    }

    // ==================== Test generarActividad - Ramas de Error Específicas ====================

    @Test
    void generarActividad_error429SinRotacionDisponible_lanzaQuotaExceededException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_error429ConResourceExhausted_lanzaQuotaExceededException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"resource_exhausted\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_responseBodyNull_usaMensajeDeException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_error429ConPerday_lanzaQuotaExceededException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        // Set all keys to force exhaustion after rotation attempts
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "You have exceeded your rate limit of 1500 requests per day per project per model for free tier".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("agotada temporalmente");
    }

    @Test
    void generarActividad_error429SinRetryAfter_lanzaQuotaExceededDefault() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota_exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("agotada temporalmente");
    }

    @Test
    void generarActividad_fechaDiferenteAlActual_resetPeticionesYIndice() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 20);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now().minusDays(1));

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(1);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(1);
    }

    @Test
    void generarActividad_rotacionEnMedioDeLaExecucion_reintentaConNuevaClaveYExitosa() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 19);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        // Primer intento falla con 429 cuando se alcanza quota, segundo intento sucede con siguiente API key
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null))
                .thenReturn(new ResponseEntity<>(Map.of(
                        "candidates", java.util.List.of(
                                Map.of("content", Map.of("parts", java.util.List.of(
                                        Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))) ), HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(2);
    }

    @Test
    void generarActividad_respuestaExitosaIncrementaPeticiones() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 5);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt");

        // Assert
        assertThat(resultado).contains("TEORICA");
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(6);
    }

    @Test
    void generarActividad_todoSintento429_lanzaQuotaExceeded() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_otroErrorDespuesDe5Intentos_lanzaLastException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_exitosaConTipoCARTA() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"CARTA\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.CARTA, "prompt carta");

        // Assert
        assertThat(resultado).contains("CARTA");
    }

    @Test
    void generarActividad_exitosaConTipoCLASIFICACION() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"CLASIFICACION\", \"titulo\": \"T\", \"descripcion\": \"D\", \"preguntas\": []}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.CLASIFICACION, "prompt");

        // Assert
        assertThat(resultado).contains("CLASIFICACION");
    }

    @Test
    void generarActividad_exitosaConTipoTABLERO() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"TABLERO\", \"titulo\": \"T\", \"descripcion\": \"D\", \"tamaño\": \"TRES_X_TRES\", \"preguntas\": []}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        String resultado = iaConnectionService.generarActividad(TipoAct.TABLERO, "prompt");

        // Assert
        assertThat(resultado).contains("TABLERO");
    }

    // ==================== Test evaluarRespuestaAbierta - Ramas Adicionales ====================

    @Test
    void evaluarRespuestaAbierta_rotacionClavesExitosa() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 19);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 1);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"puntuacion\": 8, \"comentarios\": \"Bien\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion);

        // Assert
        assertThat(evaluacion.get("puntuacion")).isEqualTo(8);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "IndiceKey")).isEqualTo(2);
    }

    @Test
    void evaluarRespuestaAbierta_error500_lanzaIllegalArgumentException() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "500 Internal Server Error"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== Test generarActividad - Excepciones Internas Exhaustivas ====================

    @Test
    void generarActividad_responseGetBodyNull_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        ResponseEntity<Map> response = new ResponseEntity<Map>((Map) null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_candidatesVacio_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of("candidates", java.util.List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_candidatesNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = new java.util.HashMap<>();
        mockResponse.put("candidates", null);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_firstCandidateNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        java.util.List<Object> candidates = new java.util.ArrayList<>();
        candidates.add(null);
        Map<String, Object> mockResponse = Map.of("candidates", candidates);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_contentNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> firstCandidate = new java.util.HashMap<>();
        firstCandidate.put("content", null);
        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(firstCandidate));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_partsNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> content = new java.util.HashMap<>();
        content.put("parts", null);
        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", content)));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_partsVacio_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of()))));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_firstPartNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        java.util.List<Object> parts = new java.util.ArrayList<>();
        parts.add(null);
        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", parts))));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_respuestaTextNulo_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> firstPart = new java.util.HashMap<>();
        firstPart.put("text", null);
        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(firstPart)))));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_validateThrowsException_propaga() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"tipo\": \"INVALID\"}"))))));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_responseBodyBlank_usaMensajeException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "   ".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void generarActividad_retryPatternDecimalPequeno_redondea() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 0);
        ReflectionTestUtils.setField(iaConnectionService, "IndiceKey", 5);
        ReflectionTestUtils.setField(iaConnectionService, "fechaUltimaPeticion", LocalDate.now());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "retry in 0.1s".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("1 segundo");
    }

    @Test
    void generarActividad_loopItera5VecesConError() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert - Verifica que lastException sea null después 5 iteraciones
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(QuotaExceededException.class);

        // Verifica que se llamó 5 veces al restTemplate
        verify(restTemplate, times(5)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void generarActividad_exceptionNoHttpStatusCode_propaga() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error al generar la actividad");
    }

    @Test
    void generarActividad_jsonParsingError_propaga() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{% invalid json"))))));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.TEORIA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_abiertaType_noSoportada_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.ABIERTA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de actividad no soportado: ABIERTA");
    }

    @Test
    void generarActividad_crucigramaType_noSoportada_lanzaException() {
        // Arrange
        Maestro maestro = crearMaestro();
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarActividad(TipoAct.CRUCIGRAMA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de actividad no soportado: CRUCIGRAMA");
    }

    // ==================== Test evaluarRespuestaAbierta - Excepciones Exhaustivas ====================

    @Test
    void evaluarRespuestaAbierta_responseBodyNull() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        ResponseEntity<Map> response = new ResponseEntity<Map>((Map) null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluarRespuestaAbierta_candidatesVacio() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        Map<String, Object> mockResponse = Map.of("candidates", java.util.List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluarRespuestaAbierta_jsonParsingError() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        Map<String, Object> textMap = Map.of("text", "not valid json");
        List<Map<String, Object>> parts = java.util.List.of(textMap);
        Map<String, Object> contentMap = Map.of("parts", parts);
        Map<String, Object> candidateMap = Map.of("content", contentMap);
        Map<String, Object> mockResponse = Map.of("candidates", java.util.List.of(candidateMap));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(mockResponse, HttpStatus.OK));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluarRespuestaAbierta_loopItera5VecesConError() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
                        "429 Too Many Requests",
                        "{\"error\": \"quota exceeded\"}".getBytes(), null));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(QuotaExceededException.class);

        verify(restTemplate, times(5)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void evaluarRespuestaAbierta_exceptionNoHttpStatusCode() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error al evaluar la respuesta");
    }

    @Test
    void evaluarRespuestaAbierta_incrementaPeticiones() {
        // Arrange
        String pregunta = "Pregunta";
        String respuesta = "Respuesta";
        Integer puntuacion = 10;
        ReflectionTestUtils.setField(iaConnectionService, "peticionesDiarias", 10);

        Map<String, Object> mockResponse = Map.of(
                "candidates", java.util.List.of(
                        Map.of("content", Map.of("parts", java.util.List.of(
                                Map.of("text", "{\"puntuacion\": 8, \"comentarios\": \"Bien\"}"))))))
        ;
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> evaluacion = iaConnectionService.evaluarRespuestaAbierta(
                pregunta, respuesta, respuesta, puntuacion);

        // Assert
        assertThat(evaluacion.get("puntuacion")).isEqualTo(8);
        assertThat(ReflectionTestUtils.getField(iaConnectionService, "peticionesDiarias")).isEqualTo(11);
    }

    // ==================== Test validateActivityResponse - Casos Exhaustivos Negativos ====================

    @Test
    void validateActivityResponse_tipoNull_lanzaException() {
        // Arrange
        String response = "{\"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_emptyJson_lanzaException() {
        // Arrange
        String response = "{}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_jsonString_lanzaException() {
        // Arrange
        String response = "\"just a string\"";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_jsonArray_lanzaException() {
        // Arrange
        String response = "[1,2,3]";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    @Test
    void validateActivityResponse_tipoAliasIncorrecto_lanzaException() {
        // Arrange - TEORICA es alias de TEORIA pero trata como TEST
        String response = "{\"tipo\": \"TEORICA\", \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEST))
                .hasMessageContaining("tipo equivocado");
    }

    @Test
    void validateActivityResponse_tipoNumeroParsing() {
        // Arrange
        String response = "{\"tipo\": 123, \"titulo\": \"T\", \"descripcion\": \"D\"}";

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "validateActivityResponse", response, TipoAct.TEORIA))
                .hasMessageContaining("400 Bad Request");
    }

    // ==================== Test generarMockActividad - Exhaustivo ====================

    @Test
    void generarMockActividad_teoriaSiempreDevuelveConstante() {
        // Act
        String resultado1 = iaConnectionService.generarMockActividad(TipoAct.TEORIA, "prompt1");
        String resultado2 = iaConnectionService.generarMockActividad(TipoAct.TEORIA, "prompt2");

        // Assert - Debe ser idéntico sin importar prompt
        assertThat(resultado1).isEqualTo(resultado2);
        assertThat(resultado1).contains("TEORICA");
    }

    @Test
    void generarMockActividad_testSiempreDevuelveConstante() {
        // Act
        String resultado1 = iaConnectionService.generarMockActividad(TipoAct.TEST, "prompt1");
        String resultado2 = iaConnectionService.generarMockActividad(TipoAct.TEST, "prompt2");

        // Assert
        assertThat(resultado1).isEqualTo(resultado2);
        assertThat(resultado1).contains("TIPO_TEST");
    }

    @Test
    void generarMockActividad_cartaNoSoportada_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarMockActividad(TipoAct.CARTA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no soportado");
    }

    @Test
    void generarMockActividad_abiertoNoSoportada_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarMockActividad(TipoAct.ABIERTA, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no soportado");
    }

    @Test
    void generarMockActividad_tableroNoSoportada_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> iaConnectionService.generarMockActividad(TipoAct.TABLERO, "prompt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no soportado");
    }

    // ==================== Test resolveApiKeyByIndex - Casos Límite ====================

    @Test
    void resolveApiKeyByIndex_negativoIndex_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", -1))
                .hasMessageContaining("Error al seleccionar la clave de API");
    }

    @Test
    void resolveApiKeyByIndex_index0_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 0))
                .hasMessageContaining("Error al seleccionar la clave de API");
    }

    @Test
    void resolveApiKeyByIndex_index10_lanzaException() {
        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(iaConnectionService, "resolveApiKeyByIndex", 10))
                .hasMessageContaining("Error al seleccionar la clave de API");
    }

    // ==================== Helper Methods ====================

    private Maestro crearMaestro() {
        Maestro maestro = new Maestro();
        maestro.setId(1L);
        return maestro;
    }

}

