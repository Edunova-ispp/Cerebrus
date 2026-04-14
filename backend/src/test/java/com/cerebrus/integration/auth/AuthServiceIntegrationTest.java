package com.cerebrus.integration.auth;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;

@SpringBootTest(properties = {
    "GOOGLE_API_KEY_1=dummy-key-1",
    "GOOGLE_API_KEY_2=dummy-key-2",
    "GOOGLE_API_KEY_3=dummy-key-3",
    "GOOGLE_API_KEY_4=dummy-key-4",
    "GOOGLE_API_KEY_5=dummy-key-5"
})
class AuthServiceIntegrationTest {

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private OrganizacionRepository organizacionRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        signupRequest = crearSignupRequest("ORGANIZACION");
    }

    @Test
    void existsByUsername_delegaEnRepositorio() {
        when(usuarioRepository.existsByNombreUsuario("usuario1")).thenReturn(true);

        boolean resultado = authService.existsByUsername("usuario1");

        assertThat(resultado).isTrue();
        verify(usuarioRepository).existsByNombreUsuario("usuario1");
    }

    @Test
    void existsByEmail_delegaEnRepositorio() {
        when(usuarioRepository.existsByCorreoElectronico("a@b.com")).thenReturn(false);

        boolean resultado = authService.existsByEmail("a@b.com");

        assertThat(resultado).isFalse();
        verify(usuarioRepository).existsByCorreoElectronico("a@b.com");
    }

    @Test
    void registrarUsuario_tipoOrganizacion_guardaUsuarioCodificado() {
        when(usuarioRepository.existsByCorreoElectronico(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encoded-pass");

        authService.registrarUsuario(signupRequest);

        verify(usuarioRepository).save(any(Usuario.class));
        verify(passwordEncoder).encode(signupRequest.getPassword());
    }

    @Test
    void registrarUsuario_tipoInvalido_lanzaIllegalArgument() {
        signupRequest.setTipoUsuario("ADMIN");

        assertThatThrownBy(() -> authService.registrarUsuario(signupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de usuario inválido");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registrarUsuario_emailExistente_lanzaIllegalArgument() {
        when(usuarioRepository.existsByCorreoElectronico(signupRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registrarUsuario(signupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo electrónico ya está en uso");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void confirmarEmail_organizacionCorrecta_marcaComoConfirmada() {
        Organizacion org = crearOrganizacion(1L, 12345678);

        when(organizacionRepository.findByCodigoVerificacion(12345678)).thenReturn(Optional.of(org));

        authService.confirmarEmail(12345678);

        assertThat(org.getEmailConfirmado()).isTrue();
        verify(usuarioRepository).save(org);
    }

    @Test
    void confirmarEmail_codigoNoEncontrado_lanzaIllegalArgument() {
        when(organizacionRepository.findByCodigoVerificacion(99999999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.confirmarEmail(99999999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de verificación no encontrado");
    }

    @Test
    void usuarioVerificado_organizacionConfirmada_devuelveTrue() {
        Organizacion org = crearOrganizacion(1L, 12345678);
        org.setEmailConfirmado(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(org));

        Boolean resultado = authService.usuarioVerificado(1L);

        assertThat(resultado).isTrue();
    }

    @Test
    void usuarioVerificado_usuarioNoOrganizacion_devuelveFalse() {
        Usuario usuario = new Maestro();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Boolean resultado = authService.usuarioVerificado(1L);

        assertThat(resultado).isFalse();
    }

    @Test
    void usuarioVerificado_usuarioNoEncontrado_lanzaIllegalArgument() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.usuarioVerificado(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    private static SignupRequest crearSignupRequest(String tipoUsuario) {
        SignupRequest request = new SignupRequest();
        request.setNombre("Ana");
        request.setPrimerApellido("Pérez");
        request.setSegundoApellido("García");
        request.setUsername("anaperez");
        request.setEmail("ana@cerebrus.com");
        request.setPassword("plain-pass");
        request.setTipoUsuario(tipoUsuario);
        request.setNombreCentro("Centro Test");
        return request;
    }

    private static Organizacion crearOrganizacion(Long id, Integer codigoVerificacion) {
        Organizacion org = new Organizacion();
        org.setId(id);
        org.setNombre("Org Test");
        org.setPrimerApellido("Apellido");
        org.setSegundoApellido("Segundo");
        org.setNombreUsuario("org" + id);
        org.setCorreoElectronico("org" + id + "@test.com");
        org.setContrasena("secret");
        org.setNombreCentro("Centro Test");
        org.setCodigoVerificacion(codigoVerificacion);
        org.setEmailConfirmado(false);
        return org;
    }
}