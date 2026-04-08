package com.cerebrus.usuario.organizacion;


import org.springframework.data.domain.Page;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;

public interface OrganizacionService {

    Page<Maestro> listarMaestros(Long organizacionId, int page, int size);
    Page<Alumno> listarAlumnos(Long organizacionId, int page, int size);
    Usuario buscarUsuario(Long organizacionId, Long usuarioId);
    void eliminarUsuario(Long organizacionId, Long usuarioId);


}
