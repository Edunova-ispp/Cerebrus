package com.cerebrus.usuario.organizacion;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.DTO.CreateUserRequest;
import com.cerebrus.usuario.organizacion.DTO.UsuarioActualizarDTO;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

public interface OrganizacionService {

    Page<Maestro> listarMaestros(Long organizacionId, int page, int size);
    Page<Alumno> listarAlumnos(Long organizacionId, int page, int size);
    Usuario buscarUsuario(Long organizacionId, Long usuarioId);
    void eliminarUsuario(Long organizacionId, Long usuarioId);
    Usuario actualizarUsuario(Long organizacionId, Long usuarioId, UsuarioActualizarDTO usuarioActualizado);
    void crearUsuario(CreateUserRequest request);
    List<String> leerArchivoImportacionUsuarios(MultipartFile archivo) throws ServletException, IOException;

}
