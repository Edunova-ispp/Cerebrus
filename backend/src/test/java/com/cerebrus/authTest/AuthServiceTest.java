package com.cerebrus.authTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Director;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

    // Captor para capturar el usuario que se guarda en el repositorio
	@Captor
	private ArgumentCaptor<Usuario> usuarioCaptor;

    // Test para verificar que el método existsByUsername delega correctamente en el repositorio
	@Test
	void existsByUsernameTest() {
		when(usuarioRepository.existsByNombreUsuario("pepe")).thenReturn(true);

		boolean exists = authService.existsByUsername("pepe");

		assertThat(exists).isTrue();
		verify(usuarioRepository).existsByNombreUsuario("pepe");
	}

    // Test para verificar que el método existsByEmail delega correctamente en el repositorio
	@Test
	void existsByEmailTest() {
		when(usuarioRepository.existsByCorreoElectronico("a@b.com")).thenReturn(false);

		boolean exists = authService.existsByEmail("a@b.com");

		assertThat(exists).isFalse();
		verify(usuarioRepository).existsByCorreoElectronico("a@b.com");
	}

    // Test para verificar que el método registrarUsuario guarda correctamente un usuario de tipo Alumno, 
    // codifica la contraseña y no guarda si el tipo de usuario no es válido
	@Test
	void registrarUsuarioAlumnoTest() {
		SignupRequest request = crearSignupRequest("ALUMNO");
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario saved = usuarioCaptor.getValue();
		assertThat(saved).isInstanceOf(Alumno.class);
		comprobarCamposComunesUsuario(saved, request);
		assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
		verify(passwordEncoder).encode(request.getPassword());
	}

    // Test para verificar que el método registrarUsuario guarda correctamente un usuario de tipo Maestro, 
    // codifica la contraseña y no guarda si el tipo de usuario no es válido
	@Test
	void registrarUsuarioMaestroTest() {
		SignupRequest request = crearSignupRequest("MAESTRO");
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario saved = usuarioCaptor.getValue();
		assertThat(saved).isInstanceOf(Maestro.class);
		comprobarCamposComunesUsuario(saved, request);
		assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
	}

    // Test para verificar que el método registrarUsuario guarda correctamente un usuario de tipo Director, 
    // codifica la contraseña y no guarda si el tipo de usuario no es válido
	@Test
	void registrarUsuarioDirectorTest() {
		SignupRequest request = crearSignupRequest("DIRECTOR");
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario saved = usuarioCaptor.getValue();
		assertThat(saved).isInstanceOf(Director.class);
		comprobarCamposComunesUsuario(saved, request);
		assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
	}

    // Test para verificar que el método registrarUsuario lanza una excepción si el tipo de usuario 
    // no es válido y no guarda nada
	@Test
	void registrarUsuarioNoValidoTest() {
		SignupRequest request = crearSignupRequest("ADMIN");

		assertThatThrownBy(() -> authService.registrarUsuario(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Tipo de usuario inválido");

		verify(usuarioRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

    // Test para verificar que al registrar un usuario, el tipo de usuario no distingue entre mayúsculas y minúsculas
	@Test
	void registrarUsuario_tipoUsuarioNoDistingueMayusculasMinusculas() {
		SignupRequest request = crearSignupRequest("alumno");
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		assertThat(usuarioCaptor.getValue()).isInstanceOf(Alumno.class);
	}

    //Método para la creación de la solicitud de registro
	private static SignupRequest crearSignupRequest(String tipoUsuario) {
		SignupRequest request = new SignupRequest();
		request.setNombre("Ana");
		request.setPrimerApellido("Pérez");
		request.setSegundoApellido("García");
		request.setUsername("anaperez");
		request.setEmail("ana@cerebrus.com");
		request.setPassword("plain-pass");
		request.setTipoUsuario(tipoUsuario);
		return request;
	}

    // Método para comprobar los campos comunes de usuario
	private static void comprobarCamposComunesUsuario(Usuario saved, SignupRequest request) {
		assertThat(saved.getNombre()).isEqualTo(request.getNombre());
		assertThat(saved.getPrimerApellido()).isEqualTo(request.getPrimerApellido());
		assertThat(saved.getSegundoApellido()).isEqualTo(request.getSegundoApellido());
		assertThat(saved.getNombreUsuario()).isEqualTo(request.getUsername());
		assertThat(saved.getCorreoElectronico()).isEqualTo(request.getEmail());
	}
}

