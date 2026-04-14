package com.cerebrus.exceptionsTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cerebrus.exceptions.QuotaExceededException;

class QuotaExceededExceptionTest {

    @Test
    void constructorConMensaje_loGuarda() {
        QuotaExceededException ex = new QuotaExceededException("Se acabó la cuota");

        assertThat(ex.getMessage()).isEqualTo("Se acabó la cuota");
    }
}
