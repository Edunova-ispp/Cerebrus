package com.cerebrus.actividadAlumnTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.cerebrus.actividadAlumn.ActividadAlumno;
import com.cerebrus.comun.enumerados.EstadoActividad;
import com.cerebrus.respuestaAlumn.respAlumGeneral.RespAlumnoGeneral;

class ActividadAlumnoEntityTest {

    @Test
    void getEstadoActividad_respuestaCorrectaSinFechaFin_permaneceEmpezada() {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(2));
        aa.setFechaFin(LocalDateTime.of(1970, 1, 1, 0, 0));

        RespAlumnoGeneral respuesta = new RespAlumnoGeneral();
        respuesta.setCorrecta(true);
        respuesta.setActividadAlumno(aa);

        aa.setRespuestasAlumno(new ArrayList<>());
        aa.getRespuestasAlumno().add(respuesta);

        assertThat(aa.getEstadoActividad()).isEqualTo(EstadoActividad.EMPEZADA);
    }

    @Test
    void getEstadoActividad_fechaFinValida_esTerminada() {
        ActividadAlumno aa = new ActividadAlumno();
        aa.setFechaInicio(LocalDateTime.now().minusMinutes(3));
        aa.setFechaFin(LocalDateTime.now());

        assertThat(aa.getEstadoActividad()).isEqualTo(EstadoActividad.TERMINADA);
    }
}
