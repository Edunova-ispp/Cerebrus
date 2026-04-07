package com.cerebrus.usuario.organizacion;

import java.util.List;

import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

public interface OrganizacionService {

    List<Maestro> listarMaestros(Long organizacionId);
    List<Alumno> listarAlumnos(Long organizacionId);


}
