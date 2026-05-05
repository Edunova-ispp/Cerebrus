package com.cerebrus.usuario.alumno;

import com.cerebrus.usuario.alumno.dto.AlumnosPageDTO;

public interface AlumnoService {

    Integer obtenerTotalPuntosAlumno();
    
    
    AlumnosPageDTO obtenerAlumnosDeOrganizacion(int numeroPagina, int tamanioPagina, String busqueda);
    
    
    AlumnosPageDTO obtenerAlumnosNoInscritosEnCurso(int numeroPagina, int tamanioPagina, String busqueda, Long cursoId);
}

