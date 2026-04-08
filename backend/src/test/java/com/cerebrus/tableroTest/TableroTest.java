package com.cerebrus.tableroTest;

import com.cerebrus.actividad.tablero.Tablero;
import com.cerebrus.comun.enumerados.TamanoTablero;
import com.cerebrus.pregunta.Pregunta;
import com.cerebrus.tema.Tema;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class TableroTest {

    @Test
    void constructorPorDefecto_y_parametrizado() {
        Tablero t1 = new Tablero();
        assertThat(t1.getPreguntas()).isEmpty();
        assertThat(t1.getTamano()).isNull();

        Tema tema = new Tema();
        Tablero t2 = new Tablero("titulo", "desc", 10, "img", true, 1, 2, tema, TamanoTablero.TRES_X_TRES);
        assertThat(t2.getTitulo()).isEqualTo("titulo");
        assertThat(t2.getDescripcion()).isEqualTo("desc");
        assertThat(t2.getPuntuacion()).isEqualTo(10);
        assertThat(t2.getImagen()).isEqualTo("img");
        assertThat(t2.getRespVisible()).isTrue();
        assertThat(t2.getPosicion()).isEqualTo(1);
        assertThat(t2.getVersion()).isEqualTo(2);
        assertThat(t2.getTema()).isSameAs(tema);
        assertThat(t2.getTamano()).isEqualTo(TamanoTablero.TRES_X_TRES);
    }

    @Test
    void gettersYSetters() {
        Tablero t = new Tablero();
        t.setTamano(TamanoTablero.CUATRO_X_CUATRO);
        assertThat(t.getTamano()).isEqualTo(TamanoTablero.CUATRO_X_CUATRO);
        List<Pregunta> preguntas = new ArrayList<>();
        t.setPreguntas(preguntas);
        assertThat(t.getPreguntas()).isSameAs(preguntas);
    }

    @Test
    void validarNumeroPreguntas_tresYTres() {
        Tablero t = new Tablero();
        t.setTamano(TamanoTablero.TRES_X_TRES);
        List<Pregunta> preguntas = new ArrayList<>();
        for(int i=0;i<8;i++) preguntas.add(new Pregunta());
        t.setPreguntas(preguntas);
        assertThat(t.validarNumeroPreguntas()).isTrue();
        t.getPreguntas().remove(0);
        assertThat(t.validarNumeroPreguntas()).isFalse();
    }

    @Test
    void validarNumeroPreguntas_cuatroYCuatro() {
        Tablero t = new Tablero();
        t.setTamano(TamanoTablero.CUATRO_X_CUATRO);
        List<Pregunta> preguntas = new ArrayList<>();
        for(int i=0;i<15;i++) preguntas.add(new Pregunta());
        t.setPreguntas(preguntas);
        assertThat(t.validarNumeroPreguntas()).isTrue();
        t.getPreguntas().add(new Pregunta());
        assertThat(t.validarNumeroPreguntas()).isFalse();
    }

    @Test
    void validarNumeroPreguntas_otroCaso() {
        Tablero t = new Tablero();
        t.setTamano(null);
        t.setPreguntas(new ArrayList<>());
        assertThat(t.validarNumeroPreguntas()).isFalse();
    }

    @Test
    void toStringIncluyeCamposPrincipales() {
        Tablero t = new Tablero();
        t.setTamano(TamanoTablero.TRES_X_TRES);
        t.setPreguntas(new ArrayList<>());
        String s = t.toString();
        assertThat(s).contains("Tablero");
        assertThat(s).contains("tamano=TRES_X_TRES");
    }
}
