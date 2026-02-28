package com.cerebrus.curso;

public class ProgresoDTO {

    private String estado; // SIN_EMPEZAR | EMPEZADA | TERMINADA
    private Integer puntos;

    public ProgresoDTO(String estado, Integer puntos) {
        this.estado = estado;
        this.puntos = puntos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }
}
