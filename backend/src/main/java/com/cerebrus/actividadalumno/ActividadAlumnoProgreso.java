package com.cerebrus.actividadAlumno;

import java.time.LocalDateTime;

public interface ActividadAlumnoProgreso {
    Long getActividadId();
    LocalDateTime getInicio();
    LocalDateTime getAcabada();
    Integer getPuntuacion();
}