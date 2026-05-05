package com.cerebrus.exceptionsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;

import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.exceptions.QuotaExceededException;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleMethodArgumentNotValid_agregaErroresYCombinaDuplicados() throws Exception {
        MethodParameter parameter = methodParameter("dummyMethod", String.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "nombre", "El nombre es obligatorio"));
        bindingResult.addError(new FieldError("request", "nombre", "El nombre no puede estar vacío"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        Map<?, ?> body = castMap(handler.handleMethodArgumentNotValid(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(body.get("mensaje")).isEqualTo("Error de validación: Los datos proporcionados no son válidos");
        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("nombre")).isEqualTo("El nombre es obligatorio; El nombre no puede estar vacío");
    }

    @Test
    void handleConstraintViolation_agregaErroresYCombinaDuplicados() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("usuario.email");
        when(violation1.getPropertyPath()).thenReturn(path);
        when(violation2.getPropertyPath()).thenReturn(path);
        when(violation1.getMessage()).thenReturn("No es válido");
        when(violation2.getMessage()).thenReturn("Debe tener formato email");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException ex = new ConstraintViolationException("error", violations);
        Map<?, ?> body = castMap(handler.handleConstraintViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(body.get("mensaje")).isEqualTo("Error de validación: Los datos proporcionados no son válidos");
        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("usuario.email").toString())
            .contains("No es válido")
            .contains("Debe tener formato email");
    }

    @Test
    void handleInvalidFormat_devuelveMensajeConTipoEsperadoYCampo() {
        InvalidFormatException ex = new InvalidFormatException((JsonParser) null, "bad", "abc", Integer.class);
        ex.prependPath(new Object(), "edad");

        Map<?, ?> body = castMap(handler.handleInvalidFormat(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(body.get("mensaje")).isEqualTo("Error de formato: El tipo de dato no es válido");
        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("edad")).isEqualTo("Formato inválido para el campo. Se esperaba: Integer");
    }

    @Test
    void handleIllegalArgument_cuandoMensajeEsNull_usaMensajePorDefecto() {
        Map<?, ?> body = castMap(handler.handleIllegalArgument(new IllegalArgumentException(), mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(body.get("mensaje")).isEqualTo("Datos inválidos");
    }

    @Test
    void handleIllegalArgument_cuandoHayMensaje_loPropaga() {
        Map<?, ?> body = castMap(handler.handleIllegalArgument(new IllegalArgumentException("No válido"), mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("No válido");
    }

    @Test
    void handleResourceNotFound_devuelve404ConMensaje() {
        Map<?, ?> body = castMap(handler.handleResourceNotFound(new ResourceNotFoundException("No encontrado"), mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.get("mensaje")).isEqualTo("No encontrado");
    }

    @Test
    void handleQuotaExceeded_cuandoMensajeEsNull_usaMensajePorDefecto() {
        Map<?, ?> body = castMap(handler.handleQuotaExceeded(new QuotaExceededException(null), mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(body.get("mensaje")).isEqualTo("Se ha alcanzado el límite de peticiones");
    }

    @Test
    void handleQuotaExceeded_cuandoHayMensaje_loPropaga() {
        Map<?, ?> body = castMap(handler.handleQuotaExceeded(new QuotaExceededException("Límite superado"), mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Límite superado");
    }

    @Test
    void handleAccessDenied_cuandoMensajeEsNull_usaMensajePorDefecto() {
        Map<?, ?> body = castMap(handler.handleAccessDenied(new AccessDeniedException(null), mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(body.get("mensaje")).isEqualTo("Acceso denegado");
    }

    @Test
    void handleAccessDenied_cuandoHayMensaje_loPropaga() {
        Map<?, ?> body = castMap(handler.handleAccessDenied(new AccessDeniedException("Sin permiso"), mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Sin permiso");
    }

    @Test
    void handleDataIntegrityViolation_detectaDataTruncation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException("Data truncation: value too long"));

        Map<?, ?> body = castMap(handler.handleDataIntegrityViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Algún campo excede el tamaño máximo permitido. Reduce el texto o el valor e inténtalo de nuevo.");
    }

    @Test
    void handleDataIntegrityViolation_detectaOutOfRange() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException("Out of range value"));

        Map<?, ?> body = castMap(handler.handleDataIntegrityViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("El valor numérico introducido es demasiado grande.");
    }

    @Test
    void handleDataIntegrityViolation_detectaDuplicate() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException("Duplicate entry"));

        Map<?, ?> body = castMap(handler.handleDataIntegrityViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Ya existe un registro con esos datos.");
    }

    @Test
    void handleDataIntegrityViolation_casoGeneral() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException("Other error"));

        Map<?, ?> body = castMap(handler.handleDataIntegrityViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Los datos introducidos no son válidos. Revisa los campos e inténtalo de nuevo.");
    }

    @Test
    void handleDataIntegrityViolation_cuandoElMensajeRaizEsNull_usaCasoGeneral() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException((String) null));

        Map<?, ?> body = castMap(handler.handleDataIntegrityViolation(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Los datos introducidos no son válidos. Revisa los campos e inténtalo de nuevo.");
    }

    @Test
    void handleHttpMessageNotReadable_detectaNumeroDemasiadoGrande() {
        InvalidFormatException cause = new InvalidFormatException((JsonParser) null, "bad", "999999999999", Integer.class);
        cause.prependPath(new Object(), "puntos");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", cause, null);

        Map<?, ?> body = castMap(handler.handleHttpMessageNotReadable(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(body.get("mensaje")).isEqualTo("El valor numérico de 'puntos' es demasiado grande.");
    }

    @Test
    void handleHttpMessageNotReadable_casoGeneral() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", new RuntimeException("otro"), null);

        Map<?, ?> body = castMap(handler.handleHttpMessageNotReadable(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Los datos enviados no tienen un formato válido. Revisa los campos e inténtalo de nuevo.");
    }

    @Test
    void handleHttpMessageNotReadable_cuandoElFormatoNoEsNumerico_caeEnCasoGeneral() {
        InvalidFormatException cause = new InvalidFormatException((JsonParser) null, "bad", "abc", String.class);
        cause.prependPath(new Object(), "campo");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", cause, null);

        Map<?, ?> body = castMap(handler.handleHttpMessageNotReadable(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("mensaje")).isEqualTo("Los datos enviados no tienen un formato válido. Revisa los campos e inténtalo de nuevo.");
    }

    @Test
    void handleMissingServletRequestParameter_devuelveBadRequestConError() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("organizacionId", "Long");

        Map<?, ?> body = castMap(handler.handleMissingServletRequestParameter(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.get("mensaje")).isEqualTo("Faltan parámetros obligatorios en la petición");
        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("organizacionId")).isEqualTo("Parámetro obligatorio ausente");
    }

    @Test
    void handleMethodArgumentTypeMismatch_cuandoHayTipoEsperado() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Long.class, "organizacionId", null, null);

        Map<?, ?> body = castMap(handler.handleMethodArgumentTypeMismatch(ex, mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.get("mensaje")).isEqualTo("Formato de parámetro inválido");
        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("organizacionId")).isEqualTo("Valor inválido. Se esperaba: Long");
    }

    @Test
    void handleMethodArgumentTypeMismatch_cuandoNoHayTipoEsperado() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", null, "organizacionId", null, null);

        Map<?, ?> body = castMap(handler.handleMethodArgumentTypeMismatch(ex, mock(WebRequest.class)).getBody());

        Map<?, ?> errores = castMap(body.get("errores"));
        assertThat(errores.get("organizacionId")).isEqualTo("Valor inválido. Se esperaba: tipo esperado");
    }

    @Test
    void handleGlobalException_devuelveErrorInterno() {
        Map<?, ?> body = castMap(handler.handleGlobalException(new RuntimeException("boom"), mock(WebRequest.class)).getBody());

        assertThat(body.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.get("mensaje")).isEqualTo("Error interno del servidor");
    }

    @Test
    void resourceNotFoundException_mensajeFormateado_yStatusAnotado() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Curso", "id", 7L);

        assertThat(ex.getMessage()).isEqualTo("No se ha encontrado una entidad del tipo Curso con campo id: '7'");
        assertThat(ex.getClass().getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void quotaExceededException_guardaElMensaje() {
        QuotaExceededException ex = new QuotaExceededException("Límite agotado");

        assertThat(ex.getMessage()).isEqualTo("Límite agotado");
    }

    private static MethodParameter methodParameter(String methodName, Class<?> parameterType) throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String value) {
        // Solo para construir MethodParameter en los tests.
    }
}
