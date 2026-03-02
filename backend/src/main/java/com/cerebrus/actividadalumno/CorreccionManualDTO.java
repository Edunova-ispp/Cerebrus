package com.cerebrus.actividadalumno;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CorreccionManualDTO {

    private Integer nuevaNota;

    private List<Long> nuevasCorreccionesRespuestasIds;

    public CorreccionManualDTO(Integer nuevaNota, List<Long> nuevasCorreccionesRespuestasIds) {
        this.nuevaNota = nuevaNota;
        this.nuevasCorreccionesRespuestasIds = nuevasCorreccionesRespuestasIds;
    }

}