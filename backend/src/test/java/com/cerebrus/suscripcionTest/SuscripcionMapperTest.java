package com.cerebrus.suscripcionTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.mapper.SuscripcionMapper;
import com.cerebrus.usuario.organizacion.Organizacion;

class SuscripcionMapperTest {

    @Test
    void toSuscripcionDTO_cuandoSuscripcionEsNull_devuelveNull() {
        assertThat(SuscripcionMapper.toSuscripcionDTO(null)).isNull();
    }

    @Test
    void toSuscripcionDTO_mapeaCamposYEstadoActivo() {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(10L);
        suscripcion.setNumMaestros(2);
        suscripcion.setNumAlumnos(3);
        suscripcion.setPrecio(99.99);
        suscripcion.setFechaInicio(LocalDate.now().minusDays(1));
        suscripcion.setFechaFin(LocalDate.now().plusDays(1));
        suscripcion.setEstadoPagoSuscripcion(EstadoPagoSuscripcion.PAGADA);
        suscripcion.setOrganizacion(new Organizacion());

        SuscripcionDTO dto = SuscripcionMapper.toSuscripcionDTO(suscripcion);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getNumMaestros()).isEqualTo(2);
        assertThat(dto.getNumAlumnos()).isEqualTo(3);
        assertThat(dto.getPrecio()).isEqualTo(99.99);
        assertThat(dto.isActiva()).isTrue();
        assertThat(dto.getEstadoPago()).isEqualTo(EstadoPagoSuscripcion.PAGADA);
    }

    @Test
    void toSuscripcion_cuandoDtoEsNull_devuelveNull() {
        assertThat(SuscripcionMapper.toSuscripcion(null)).isNull();
    }

    @Test
    void toSuscripcion_mapeaCamposBasicos() {
        SuscripcionDTO dto = new SuscripcionDTO(10L, 2, 3, 99.99, LocalDate.now(), LocalDate.now().plusDays(2), true, EstadoPagoSuscripcion.PAGADA);

        Suscripcion suscripcion = SuscripcionMapper.toSuscripcion(dto);

        assertThat(suscripcion.getId()).isEqualTo(10L);
        assertThat(suscripcion.getNumMaestros()).isEqualTo(2);
        assertThat(suscripcion.getNumAlumnos()).isEqualTo(3);
        assertThat(suscripcion.getPrecio()).isEqualTo(99.99);
        assertThat(suscripcion.getFechaInicio()).isEqualTo(dto.getFechaInicio());
        assertThat(suscripcion.getFechaFin()).isEqualTo(dto.getFechaFin());
    }
}
