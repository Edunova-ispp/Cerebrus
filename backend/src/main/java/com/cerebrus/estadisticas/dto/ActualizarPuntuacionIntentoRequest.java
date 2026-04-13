package com.cerebrus.estadisticas.dto;

public class ActualizarPuntuacionIntentoRequest {

    private Integer puntuacion;

    public ActualizarPuntuacionIntentoRequest() {}

    public ActualizarPuntuacionIntentoRequest(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }
}
