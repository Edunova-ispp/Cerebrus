package com.cerebrus.iaconnectionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cerebrus.comun.enumerados.TipoAct;
import com.cerebrus.iaconnection.IaConnectionController;
import com.cerebrus.iaconnection.IaConnectionService;

@ExtendWith(MockitoExtension.class)
class IaConnectionControllerTest {

    @Mock
    private IaConnectionService iaConnectionService;

    @InjectMocks
    private IaConnectionController iaConnectionController;

    // ==================== Test generarMockActividad ====================

    @Test
    void generarMockActividad_requestValidoTipoTEORIA_devuelveOk() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt sobre teoría");
        request.setDescripcion("Descripción");

        String mockJson = "{\"tipo\": \"TEORICA\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockJson);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.TEORIA), eq("Prompt sobre teoría"));
    }

    @Test
    void generarMockActividad_requestValidoTipoTEST_devuelveOk() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEST");
        request.setPrompt("Preguntas test");

        String mockJson = "{\"tipo\": \"TEST\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEST), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockJson);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.TEST), eq("Preguntas test"));
    }

    @Test
    void generarMockActividad_requestValidoTipoORDEN_devuelveOk() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("ORDEN");
        request.setPrompt("Orden elementos");

        String mockJson = "{\"tipo\": \"ORDEN\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.ORDEN), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.ORDEN), eq("Orden elementos"));
    }

    @Test
    void generarMockActividad_promptNullUsaDescripcion_devuelveOk() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt(null);
        request.setDescripcion("Descripción como prompt");

        String mockJson = "{\"tipo\": \"TEORICA\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.TEORIA), eq("Descripción como prompt"));
    }

    @Test
    void generarMockActividad_tipoActividadInvalido_devuelveBadRequest() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TIPO_INVALIDO");
        request.setPrompt("Prompt");

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void generarMockActividad_requestCausaExcepcion_devuelveBadRequest() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt");

        when(iaConnectionService.generarMockActividad(any(), any()))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generarMockActividad_toUpperCaseEnTipo_funcionaCorrecto() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("teoria");
        request.setPrompt("Prompt en minúscula");

        String mockJson = "{\"tipo\": \"TEORICA\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.TEORIA), any());
    }

    // ==================== Test generarActividad ====================

    @Test
    void generarActividad_requestValidoTipoTEORIA_devuelveOk() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt para generar actividad");
        request.setDescripcion("Descripción");

        String json = "{\"tipo\": \"TEORICA\", \"titulo\": \"Título\", \"descripcion\": \"Desc\"}";
        when(iaConnectionService.generarActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(json);
        verify(iaConnectionService).generarActividad(eq(TipoAct.TEORIA), eq("Prompt para generar actividad"));
    }

    @Test
    void generarActividad_requestValidoTipoTEST_devuelveOk() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TEST");
        request.setPrompt("Generar preguntas");

        String json = "{\"tipo\": \"TEST\", \"preguntas\": []}";
        when(iaConnectionService.generarActividad(eq(TipoAct.TEST), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarActividad(eq(TipoAct.TEST), eq("Generar preguntas"));
    }

    @Test
    void generarActividad_promptNullUsaDescripcion_devuelveOk() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("ORDEN");
        request.setPrompt(null);
        request.setDescripcion("Descripción orden");

        String json = "{\"tipo\": \"ORDEN\", \"valores\": []}";
        when(iaConnectionService.generarActividad(eq(TipoAct.ORDEN), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarActividad(eq(TipoAct.ORDEN), eq("Descripción orden"));
    }

    @Test
    void generarActividad_tipoActividadInvalido_lanzaException() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TIPO_NO_VALIDO");
        request.setPrompt("Prompt");

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionController.generarActividad(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_usuarioNoEsMaestro_lanzaException() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt");

        when(iaConnectionService.generarActividad(any(), any()))
                .thenThrow(new IllegalArgumentException("403 Forbidden: El usuario no es un maestro"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionController.generarActividad(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarActividad_serviceLanzaQuotaExceededException() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt");

        when(iaConnectionService.generarActividad(any(), any()))
                .thenThrow(new com.cerebrus.exceptions.QuotaExceededException("Cuota excedida"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionController.generarActividad(request))
                .isInstanceOf(com.cerebrus.exceptions.QuotaExceededException.class);
    }

    @Test
    void generarActividad_toUpperCaseEnTipo_funcionaCorrecto() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("test");
        request.setPrompt("test de minúsculas");

        String json = "{\"tipo\": \"TEST\"}";
        when(iaConnectionService.generarActividad(eq(TipoAct.TEST), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarActividad(eq(TipoAct.TEST), any());
    }

    // ==================== Test casos límite y negativos ====================

    @Test
    void generarMockActividad_todosLosTiposDeActividad() {
        // Arrange & Act & Assert
        String[] tipos = {"TEORIA", "TEST", "ORDEN", "CARTA", "CRUCIGRAMA", "ABIERTA", "CLASIFICACION", "TABLERO", "IMAGEN"};

        for (String tipo : tipos) {
            IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
            request.setTipoActividad(tipo);
            request.setPrompt("prompt para " + tipo);

            String mockJson = "{\"tipo\": \"" + tipo + "\"}";
            when(iaConnectionService.generarMockActividad(any(TipoAct.class), any(String.class)))
                    .thenReturn(mockJson);

            // Act
            ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void generarMockActividad_promptYDescripcionAmbosPresentes_usaPrompt() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt prioritario");
        request.setDescripcion("Descripción ignorada");

        String mockJson = "{\"tipo\": \"TEORICA\"}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarMockActividad(eq(TipoAct.TEORIA), eq("Prompt prioritario"));
    }

    @Test
    void generarMockActividad_respuestaVacia_devuelveOk() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("prompt");

        String mockJson = "";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEORIA), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void generarMockActividad_respuestaCompleja_preservaFormato() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEST");
        request.setPrompt("Test complejo");

        String mockJson = "{\"tipo\": \"TIPO_TEST\", \"titulo\": \"Título\", \"descripcion\": \"Desc\", \"preguntas\": [{\"enunciado\": \"Pregunta\", \"opciones\": []}]}";
        when(iaConnectionService.generarMockActividad(eq(TipoAct.TEST), any(String.class)))
                .thenReturn(mockJson);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("preguntas");
    }

    @Test
    void generarActividad_respuestaCompleja_preservaFormato() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("ORDEN");
        request.setPrompt("Generar órden");

        String json = "{\"tipo\": \"ORDEN\", \"titulo\": \"Título\", \"descripcion\": \"Desc\", \"valores\": [{\"texto\": \"Elemento\", \"orden\": 1}]}";
        when(iaConnectionService.generarActividad(eq(TipoAct.ORDEN), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("valores");
    }

    @Test
    void generarActividad_tipoActividadMezclado_convierteAUpperCase() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("OrDeN");
        request.setPrompt("Mezclado");

        String json = "{\"tipo\": \"ORDEN\"}";
        when(iaConnectionService.generarActividad(eq(TipoAct.ORDEN), any(String.class)))
                .thenReturn(json);

        // Act
        ResponseEntity<String> response = iaConnectionController.generarActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iaConnectionService).generarActividad(eq(TipoAct.ORDEN), any());
    }

    @Test
    void generarMockActividad_serviceLanzaQuotaExceededException() {
        // Arrange
        IaConnectionController.MockIaRequest request = new IaConnectionController.MockIaRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt");

        when(iaConnectionService.generarMockActividad(any(), any()))
                .thenThrow(new com.cerebrus.exceptions.QuotaExceededException("Cuota excedida"));

        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generarMockActividad_requestNull_lanzaException() {
        // Act
        ResponseEntity<String> response = iaConnectionController.generarMockActividad(null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
        verifyNoInteractions(iaConnectionService);
    }

    @Test
    void generarActividad_requestCausaOtroTipodeError() {
        // Arrange
        IaConnectionController.GenerarActividadRequest request = new IaConnectionController.GenerarActividadRequest();
        request.setTipoActividad("TEORIA");
        request.setPrompt("Prompt");

        when(iaConnectionService.generarActividad(any(), any()))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        assertThatThrownBy(() -> iaConnectionController.generarActividad(request))
                .isInstanceOf(RuntimeException.class);
    }

}
