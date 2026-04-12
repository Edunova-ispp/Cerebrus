package com.cerebrus.respuestaAlumn.respAlumOrdenacion.dto;

import java.util.List;

public class RespAlumnoOrdenacionDetalleDTO {

    private Long id;
    private Boolean correcta;
    private List<String> valoresAlum;
    private List<String> valoresCorrectos;

    public RespAlumnoOrdenacionDetalleDTO() {
    }

    public RespAlumnoOrdenacionDetalleDTO(Long id, Boolean correcta, List<String> valoresAlum, List<String> valoresCorrectos) {
        this.id = id;
        this.correcta = correcta;
        this.valoresAlum = valoresAlum;
        this.valoresCorrectos = valoresCorrectos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCorrecta() {
        return correcta;
    }

    public void setCorrecta(Boolean correcta) {
        this.correcta = correcta;
    }

    public List<String> getValoresAlum() {
        return valoresAlum;
    }

    public void setValoresAlum(List<String> valoresAlum) {
        this.valoresAlum = valoresAlum;
    }

    public List<String> getValoresCorrectos() {
        return valoresCorrectos;
    }

    public void setValoresCorrectos(List<String> valoresCorrectos) {
        this.valoresCorrectos = valoresCorrectos;
    }
}
