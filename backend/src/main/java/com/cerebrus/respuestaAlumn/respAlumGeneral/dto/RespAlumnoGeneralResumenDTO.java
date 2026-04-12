package com.cerebrus.respuestaAlumn.respAlumGeneral.dto;

public class RespAlumnoGeneralResumenDTO {

    private Long preguntaId;
    private String respuesta;
    private Boolean correcta;
    private String respuestaCorrecta;

    public RespAlumnoGeneralResumenDTO() {
    }

    public RespAlumnoGeneralResumenDTO(Long preguntaId, String respuesta, Boolean correcta, String respuestaCorrecta) {
        this.preguntaId = preguntaId;
        this.respuesta = respuesta;
        this.correcta = correcta;
        this.respuestaCorrecta = respuestaCorrecta;
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public void setPreguntaId(Long preguntaId) {
        this.preguntaId = preguntaId;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Boolean getCorrecta() {
        return correcta;
    }

    public void setCorrecta(Boolean correcta) {
        this.correcta = correcta;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public void setRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
    }
}
