package com.cerebrus.exceptionsTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cerebrus.exceptions.ResourceNotFoundException;

class ResourceNotFoundExceptionTest {

    @Test
    void constructorConMensaje_guardaElMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entidad no encontrada");

        assertThat(ex.getMessage()).isEqualTo("Entidad no encontrada");
    }

    @Test
    void constructorConCampos_formateaElMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Curso", "id", 15L);

        assertThat(ex.getMessage()).isEqualTo("No se ha encontrado una entidad del tipo Curso con campo id: '15'");
    }

    @Test
    void tieneResponseStatusNotFound() {
        assertThat(ResourceNotFoundException.class.getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
