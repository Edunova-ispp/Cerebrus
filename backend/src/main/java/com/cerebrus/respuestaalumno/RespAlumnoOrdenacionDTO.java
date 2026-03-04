package com.cerebrus.respuestaalumno;

public class RespAlumnoOrdenacionDTO {

    private Long id;
    private Boolean correcta;

    public RespAlumnoOrdenacionDTO() {
    }

    public RespAlumnoOrdenacionDTO(Long id, Boolean correcta) {
        this.id = id;
        this.correcta = correcta;
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
}
