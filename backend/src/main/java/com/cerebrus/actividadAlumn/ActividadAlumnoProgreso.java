package com.cerebrus.actividadAlumn;

import java.time.LocalDateTime;

public interface ActividadAlumnoProgreso {
    Long getActividadId();
    LocalDateTime getInicio();
    LocalDateTime getAcabada();
    Integer getPuntuacion();
}