package com.cerebrus.organizacionTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;
import com.cerebrus.usuario.organizacion.OrganizacionServiceImpl;
import com.cerebrus.usuario.organizacion.dto.CreateUserRequest;
import com.cerebrus.usuario.organizacion.dto.UsuarioActualizarDTO;

@ExtendWith(MockitoExtension.class)
class OrganizacionServiceImplTest {

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OrganizacionServiceImpl organizacionService;

    private Organizacion organizacionActiva;
    private Organizacion organizacionInactiva;
    private Maestro maestro;
    private Alumno alumno;

    @BeforeEach
    void setUp() {
        organizacionActiva = crearOrganizacion(1L, true);
        organizacionInactiva = crearOrganizacion(1L, false);

        maestro = new Maestro();
        maestro.setId(10L);
        maestro.setNombre("Mario");
        maestro.setNombreUsuario("mario10");
        maestro.setCorreoElectronico("mario@org.com");
        maestro.setContrasena("pwd");
        maestro.setOrganizacion(organizacionActiva);

        alumno = new Alumno();
        alumno.setId(20L);
        alumno.setNombre("Ana");
        alumno.setNombreUsuario("ana20");
        alumno.setCorreoElectronico("ana@org.com");
        alumno.setContrasena("pwd2");
        alumno.setPuntos(8);
        alumno.setOrganizacion(organizacionActiva);

        organizacionActiva.getMaestros().add(maestro);
        organizacionActiva.getAlumnos().add(alumno);
    }

    @Test
    void listarMaestros_organizacionActiva_retornaPaginaPaginada() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act
        Page<Maestro> page = organizacionService.listarMaestros(1L, 0, 1);

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getId()).isEqualTo(10L);
    }

    @Test
    void listarMaestros_suscripcionInactiva_lanzaAccessDenied() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionInactiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionInactiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.listarMaestros(1L, 0, 10))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no tiene una suscripción activa");
    }

    @Test
    void listarAlumnos_pageNegativa_lanzaIllegalArgumentException() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.listarAlumnos(1L, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void listarAlumnos_sizeCero_lanzaIllegalArgumentException() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.listarAlumnos(1L, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    @Test
    void buscarUsuario_existente_retornaUsuarioSeguro() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act
        Usuario resultado = organizacionService.buscarUsuario(1L, 20L);

        // Assert
        assertThat(resultado).isInstanceOf(Alumno.class);
        assertThat(resultado.getId()).isEqualTo(20L);
        assertThat(resultado).isNotSameAs(alumno);
    }

    @Test
    void buscarUsuario_noEncontrado_lanzaResourceNotFound() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.buscarUsuario(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void eliminarUsuario_existente_loBorraEnRepositorio() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act
        organizacionService.eliminarUsuario(1L, 20L);

        // Assert
        verify(usuarioRepository).deleteById(20L);
        assertThat(organizacionActiva.getAlumnos()).isEmpty();
    }

    @Test
    void eliminarUsuario_noExistente_lanzaResourceNotFound() {
        // Arrange
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.eliminarUsuario(1L, 777L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado en la organización");

        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    void actualizarUsuario_conContrasenaActualizaYGuarda() {
        // Arrange
        UsuarioActualizarDTO dto = new UsuarioActualizarDTO("N", "A1", "A2", "nuevoUser", "nuevo@mail.com", "nuevaPwd");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario actualizado = organizacionService.actualizarUsuario(1L, 20L, dto);

        // Assert
        assertThat(actualizado.getNombre()).isEqualTo("N");
        assertThat(actualizado.getNombreUsuario()).isEqualTo("nuevoUser");
        assertThat(actualizado.getContrasena()).isEqualTo("nuevaPwd");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_usernameDuplicado_lanzaIllegalArgumentException() {
        // Arrange
        Maestro otroMaestro = new Maestro();
        otroMaestro.setId(99L);
        otroMaestro.setNombreUsuario("duplicado");
        organizacionActiva.getMaestros().add(otroMaestro);

        UsuarioActualizarDTO dto = new UsuarioActualizarDTO("N", "A1", "A2", "duplicado", "nuevo@mail.com", "");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacionActiva));

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.actualizarUsuario(1L, 20L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de usuario ya está en uso");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_maestro_valido_guardaUsuario() {
        // Arrange
        CreateUserRequest request = requestBase("MAESTRO");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(usuarioRepository.existsByNombreUsuario("nuevoUser")).thenReturn(false);
        when(usuarioRepository.existsByCorreoElectronico("nuevo@org.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded");

        // Act
        organizacionService.crearUsuario(request);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(Maestro.class);
        assertThat(captor.getValue().getContrasena()).isEqualTo("encoded");
    }

    @Test
    void crearUsuario_rolOrganizacion_lanzaIllegalArgumentException() {
        // Arrange
        CreateUserRequest request = requestBase("ORGANIZACION");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.crearUsuario(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede crear usuarios con rol ORGANIZACIÓN");
    }

    @Test
    void crearUsuario_usernameDuplicado_lanzaIllegalArgumentException() {
        // Arrange
        CreateUserRequest request = requestBase("ALUMNO");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(usuarioRepository.existsByNombreUsuario("nuevoUser")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.crearUsuario(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_suscripcionInactiva_lanzaAccessDenied() {
        // Arrange
        CreateUserRequest request = requestBase("ALUMNO");
        when(usuarioService.findCurrentUser()).thenReturn(organizacionInactiva);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.crearUsuario(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("suscripción activa");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void leerArchivoImportacionUsuarios_csvValido_guardaTodosYRetornaVacio() throws Exception {
        // Arrange
        String csv = "Nombre,Primer Apellido,Segundo Apellido,Correo Electronico,Nombre de Usuario,Contrasena,Rol\n" +
                "Luis,Perez,Gomez,luis@org.com,luis,pwd,ALUMNO\n";
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(usuarioRepository.existsByNombreUsuario("luis")).thenReturn(false);
        when(usuarioRepository.existsByCorreoElectronico("luis@org.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("enc");

        // Act
        List<String> errores = organizacionService.leerArchivoImportacionUsuarios(archivo);

        // Assert
        assertThat(errores).isEmpty();
        verify(usuarioRepository).saveAll(anyList());
    }

    @Test
    void leerArchivoImportacionUsuarios_csvConUsernameRepetido_retornaErroresYNoGuarda() throws Exception {
        // Arrange
        String csv = "Nombre,Primer Apellido,Segundo Apellido,Correo Electronico,Nombre de Usuario,Contrasena,Rol\n" +
                "Luis,Perez,Gomez,luis1@org.com,luis,pwd,ALUMNO\n" +
                "Ana,Perez,Gomez,ana1@org.com,luis,pwd2,MAESTRO\n";
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);
        when(usuarioRepository.existsByNombreUsuario(any())).thenReturn(false);
        when(usuarioRepository.existsByCorreoElectronico(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("enc");

        // Act
        List<String> errores = organizacionService.leerArchivoImportacionUsuarios(archivo);

        // Assert
        assertThat(errores).isNotEmpty();
        assertThat(errores.stream().anyMatch(e -> e.contains("username") && e.contains("repetido"))).isTrue();
        verify(usuarioRepository, never()).saveAll(anyList());
    }

    @Test
    void leerArchivoImportacionUsuarios_formatoNoSoportado_lanzaIllegalArgument() {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8));
        when(usuarioService.findCurrentUser()).thenReturn(organizacionActiva);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.leerArchivoImportacionUsuarios(archivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Archivo no soportado");
    }

    @Test
    void leerArchivoImportacionUsuarios_usuarioNoOrganizacion_lanzaClassCast() {
        // Arrange
        Usuario noOrg = new Usuario() {};
        noOrg.setId(2L);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "usuarios.csv", "text/csv", "x".getBytes(StandardCharsets.UTF_8));
        when(usuarioService.findCurrentUser()).thenReturn(noOrg);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.leerArchivoImportacionUsuarios(archivo))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void validarPropietaria_usuarioNoCoincide_lanzaIllegalArgumentException() {
        // Arrange
        Organizacion otra = crearOrganizacion(99L, true);
        when(usuarioService.findCurrentUser()).thenReturn(otra);

        // Act + Assert
        assertThatThrownBy(() -> organizacionService.listarMaestros(1L, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo la organización propietaria");

        verify(organizacionRepository, never()).findById(any());
    }

    private Organizacion crearOrganizacion(Long id, boolean activa) {
        Organizacion org = new Organizacion();
        org.setId(id);
        org.setNombre("Org");
        org.setMaestros(new ArrayList<>());
        org.setAlumnos(new ArrayList<>());
        org.setSuscripciones(new ArrayList<>());

        LocalDate hoy = LocalDate.now();
        if (activa) {
            Suscripcion s = new Suscripcion();
            s.setFechaInicio(hoy.minusDays(1));
            s.setFechaFin(hoy.plusDays(30));
            s.setEstadoPagoSuscripcion(EstadoPagoSuscripcion.PAGADA);
            org.getSuscripciones().add(s);
        } else {
            Suscripcion s = new Suscripcion();
            s.setFechaInicio(hoy.minusDays(30));
            s.setFechaFin(hoy.minusDays(1));
            s.setEstadoPagoSuscripcion(EstadoPagoSuscripcion.PAGADA);
            org.getSuscripciones().add(s);
        }
        return org;
    }

    private CreateUserRequest requestBase(String rol) {
        CreateUserRequest request = new CreateUserRequest();
        request.setNombre("Nuevo");
        request.setPrimerApellido("Apellido1");
        request.setSegundoApellido("Apellido2");
        request.setUsername("nuevoUser");
        request.setEmail("nuevo@org.com");
        request.setPassword("1234");
        request.setRol(rol);
        return request;
    }
}