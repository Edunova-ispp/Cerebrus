package com.cerebrus.organizacionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.OrganizacionController;
import com.cerebrus.usuario.organizacion.OrganizacionService;
import com.cerebrus.usuario.organizacion.dto.CreateUserRequest;
import com.cerebrus.usuario.organizacion.dto.UsuarioActualizarDTO;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class OrganizacionControllerTest {

    @Mock
    private OrganizacionService organizacionService;

    @InjectMocks
    private OrganizacionController organizacionController;

    private Maestro maestro;
    private Alumno alumno;

    @BeforeEach
    void setUp() {
        maestro = new Maestro();
        maestro.setId(10L);
        maestro.setNombre("Maestro 1");

        alumno = new Alumno();
        alumno.setId(20L);
        alumno.setNombre("Alumno 1");
    }

    @Test
    void listarMaestros_delegaEnServicioYRetornaPagina() {
        // Arrange
        org.springframework.data.domain.Page<Maestro> page = new org.springframework.data.domain.PageImpl<>(List.of(maestro));
        when(organizacionService.listarMaestros(1L, 0, 10)).thenReturn(page);

        // Act
        org.springframework.data.domain.Page<Maestro> resultado = organizacionController.listarMaestros(1L, 0, 10);

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().getId()).isEqualTo(10L);
        verify(organizacionService).listarMaestros(1L, 0, 10);
    }

    @Test
    void listarAlumnos_delegaEnServicioYRetornaPagina() {
        // Arrange
        org.springframework.data.domain.Page<Alumno> page = new org.springframework.data.domain.PageImpl<>(List.of(alumno));
        when(organizacionService.listarAlumnos(1L, 1, 5)).thenReturn(page);

        // Act
        org.springframework.data.domain.Page<Alumno> resultado = organizacionController.listarAlumnos(1L, 1, 5);

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().getId()).isEqualTo(20L);
        verify(organizacionService).listarAlumnos(1L, 1, 5);
    }

    @Test
    void buscarUsuario_delegaEnServicioYRetornaUsuario() {
        // Arrange
        when(organizacionService.buscarUsuario(1L, 20L)).thenReturn(alumno);

        // Act
        Usuario resultado = organizacionController.buscarUsuario(1L, 20L);

        // Assert
        assertThat(resultado).isSameAs(alumno);
        verify(organizacionService).buscarUsuario(1L, 20L);
    }

    @Test
    void eliminarUsuario_delegaEnServicio() {
        // Arrange / Act
        organizacionController.eliminarUsuario(1L, 20L);

        // Assert
        verify(organizacionService).eliminarUsuario(1L, 20L);
    }

    @Test
    void actualizarUsuario_delegaEnServicioYRetornaUsuario() {
        // Arrange
        UsuarioActualizarDTO dto = new UsuarioActualizarDTO();
        dto.setNombre("Nuevo");
        when(organizacionService.actualizarUsuario(1L, 20L, dto)).thenReturn(alumno);

        // Act
        Usuario resultado = organizacionController.actualizarUsuario(1L, 20L, dto);

        // Assert
        assertThat(resultado).isSameAs(alumno);
        verify(organizacionService).actualizarUsuario(1L, 20L, dto);
    }

    @Test
    void crearUsuario_valido_retorna201() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setRol("ALUMNO");

        // Act
        ResponseEntity<String> respuesta = organizacionController.crearUsuario(request);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isEqualTo("Usuario creado correctamente");
        verify(organizacionService).crearUsuario(request);
    }

    @Test
    void importarUsuarios_conErroresDeValidacion_retorna400() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenReturn(List.of("error 1", "error 2"));

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).contains("Errores encontrados en el archivo");
    }

    @Test
    void importarUsuarios_sinErrores_retorna200() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenReturn(List.of());

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo("Archivo importado correctamente");
    }

    @Test
    void importarUsuarios_accessDenied_retorna403() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenThrow(new AccessDeniedException("Sin permisos"));

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(respuesta.getBody()).isEqualTo("Sin permisos");
    }

    @Test
    void importarUsuarios_servletException_retorna500() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenThrow(new ServletException("Fallo interno"));

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).isEqualTo("Fallo interno");
    }

    @Test
    void importarUsuarios_illegalArgument_retorna400() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenThrow(new IllegalArgumentException("Formato incorrecto"));

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isEqualTo("Formato incorrecto");
    }

    @Test
    void importarUsuarios_errorGenerico_retorna400ConPrefijo() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes());
        when(organizacionService.leerArchivoImportacionUsuarios(archivo)).thenThrow(new RuntimeException("boom"));

        // Act
        ResponseEntity<String> respuesta = organizacionController.importarUsuarios(archivo);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isEqualTo("Error inesperado: boom");
    }
}
