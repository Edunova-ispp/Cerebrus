package com.cerebrus.actividadAlumno;

import java.time.LocalDateTime;

public interface ActividadAlumnoProgreso {
    LocalDateTime getInicio();
    LocalDateTime getAcabada();
    Integer getPuntuacion();
}