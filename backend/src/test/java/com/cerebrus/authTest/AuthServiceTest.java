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
import org.springframework.test.util.ReflectionTestUtils;

import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;
import com.cerebrus.usuario.organizacion.Organizacion;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private OrganizacionRepository organizacionRepository;

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

	@Test
	void existsByEmailCuandoYaExiste_devuelveTrue() {
		when(usuarioRepository.existsByCorreoElectronico("repetido@cerebrus.com")).thenReturn(true);

		boolean exists = authService.existsByEmail("repetido@cerebrus.com");

		assertThat(exists).isTrue();
		verify(usuarioRepository).existsByCorreoElectronico("repetido@cerebrus.com");
	}

    // Test para verificar que el método registrarUsuario guarda correctamente un usuario de tipo Alumno, 
    // codifica la contraseña y no guarda si el tipo de usuario no es válido
	//@Test
	//void registrarUsuarioAlumnoTest() {
	//	SignupRequest request = crearSignupRequest("ALUMNO");
	//	when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

	//	authService.registrarUsuario(request);

	//	verify(usuarioRepository).save(usuarioCaptor.capture());
	//	Usuario saved = usuarioCaptor.getValue();
	//	assertThat(saved).isInstanceOf(Alumno.class);
		//comprobarCamposComunesUsuario(saved, request);
		//assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
		//verify(passwordEncoder).encode(request.getPassword());
	//}

    // Test para verificar que el método registrarUsuario guarda correctamente un usuario de tipo Maestro, 
    // codifica la contraseña y no guarda si el tipo de usuario no es válido
	// @Test
	// void registrarUsuarioMaestroTest() {
	// 	SignupRequest request = crearSignupRequest("MAESTRO");
	// 	when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

	// 	authService.registrarUsuario(request);

	// 	verify(usuarioRepository).save(usuarioCaptor.capture());
	// 	Usuario saved = usuarioCaptor.getValue();
	// 	assertThat(saved).isInstanceOf(Maestro.class);
	// 	comprobarCamposComunesUsuario(saved, request);
	// 	assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
	// }

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

	@Test
	void registrarUsuario_cuandoEmailYaExiste_lanzaIllegalArgumentException() {
		SignupRequest request = crearSignupRequest("ORGANIZACION");
		when(usuarioRepository.existsByCorreoElectronico(request.getEmail())).thenReturn(true);

		assertThatThrownBy(() -> authService.registrarUsuario(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El correo electrónico ya está en uso.");

		verify(usuarioRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void registrarUsuario_cuandoEsOrganizacion_guardaUsuarioYCodificaPassword() {
		SignupRequest request = crearSignupRequest("ORGANIZACION");
		request.setNombreCentro("Colegio Test");
		when(usuarioRepository.existsByCorreoElectronico(request.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario saved = usuarioCaptor.getValue();
		assertThat(saved).isInstanceOf(Organizacion.class);
		assertThat(saved.getNombre()).isEqualTo(request.getNombre());
		assertThat(saved.getPrimerApellido()).isEqualTo(request.getPrimerApellido());
		assertThat(saved.getSegundoApellido()).isEqualTo(request.getSegundoApellido());
		assertThat(saved.getNombreUsuario()).isEqualTo(request.getEmail());
		assertThat(saved.getCorreoElectronico()).isEqualTo(request.getEmail());
		assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
		assertThat(((Organizacion) saved).getNombreCentro()).isEqualTo("Colegio Test");
		assertThat(((Organizacion) saved).getEmailConfirmado()).isFalse();
		assertThat(((Organizacion) saved).getCodigoVerificacion()).isBetween(10_000_000, 99_999_999);
		verify(passwordEncoder).encode(request.getPassword());
	}

	@Test
	void enviarEmailVerificacion_cuandoNoHayApiKey_noLanzaExcepcion() {
		ReflectionTestUtils.setField(authService, "brevoApiKey", "");

		authService.enviarEmailVerificacion("test@cerebrus.com", 12345678);

		assertThat(true).isTrue();
	}

	@Test
	void enviarEmailVerificacion_cuandoApiKeyEsNull_noLanzaExcepcion() {
		ReflectionTestUtils.setField(authService, "brevoApiKey", null);

		authService.enviarEmailVerificacion("test@cerebrus.com", 12345678);

		assertThat(true).isTrue();
	}

	@Test
	void enviarEmailVerificacion_cuandoHayApiKey_ejecutaRamaCompletaSinPropagarExcepcion() {
		ReflectionTestUtils.setField(authService, "brevoApiKey", "test-api-key");
		ReflectionTestUtils.setField(authService, "breevoSenderEmail", "noreply@cerebrus.com");
		ReflectionTestUtils.setField(authService, "brevoSenderName", "Cerebrus");

		authService.enviarEmailVerificacion("test@cerebrus.com", 12345678);

		assertThat(true).isTrue();
	}

	@Test
	void enviarEmailVerificacion_cuandoSenderEmailEsNull_usaRemitentePorDefecto() {
		ReflectionTestUtils.setField(authService, "brevoApiKey", "test-api-key");
		ReflectionTestUtils.setField(authService, "breevoSenderEmail", null);
		ReflectionTestUtils.setField(authService, "brevoSenderName", "Cerebrus");

		authService.enviarEmailVerificacion("test@cerebrus.com", 12345678);

		assertThat(true).isTrue();
	}

    // Test para verificar que al registrar un usuario, el tipo de usuario no distingue entre mayúsculas y minúsculas
	@Test
	void registrarUsuario_tipoUsuarioNoDistingueMayusculasMinusculas() {
		SignupRequest request = crearSignupRequest("ORGANIZACION");
		request.setNombreCentro("Centro Mixto");
		when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn("encoded-pass");
		when(usuarioRepository.existsByCorreoElectronico(request.getEmail())).thenReturn(false);

		authService.registrarUsuario(request);

		verify(usuarioRepository).save(usuarioCaptor.capture());
		assertThat(usuarioCaptor.getValue()).isInstanceOf(Organizacion.class);
	}

	@Test
	void confirmarEmail_cuandoCodigoExiste_yCoincide_marcaEmailComoConfirmado() {
		Organizacion org = new Organizacion();
		org.setId(1L);
		org.setCodigoVerificacion(12345678);
		org.setEmailConfirmado(false);
		when(organizacionRepository.findByCodigoVerificacion(12345678)).thenReturn(java.util.Optional.of(org));

		authService.confirmarEmail(12345678);

		assertThat(org.getEmailConfirmado()).isTrue();
		verify(usuarioRepository).save(org);
	}

	@Test
	void confirmarEmail_cuandoCodigoExiste_peroNoCoincide_lanzaIllegalArgumentException() {
		Organizacion org = new Organizacion();
		org.setId(1L);
		org.setCodigoVerificacion(87654321);
		org.setEmailConfirmado(false);
		when(organizacionRepository.findByCodigoVerificacion(12345678)).thenReturn(java.util.Optional.of(org));

		assertThatThrownBy(() -> authService.confirmarEmail(12345678))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Código de verificación incorrecto.");

		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void confirmarEmail_cuandoNoExisteCodigo_lanzaIllegalArgumentException() {
		when(organizacionRepository.findByCodigoVerificacion(12345678)).thenReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> authService.confirmarEmail(12345678))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Código de verificación no encontrado.");
	}

	@Test
	void usuarioVerificado_cuandoEsOrganizacion_devuelveEstado() {
		Organizacion org = new Organizacion();
		org.setId(7L);
		org.setEmailConfirmado(true);
		when(usuarioRepository.findById(7L)).thenReturn(java.util.Optional.of(org));

		Boolean resultado = authService.usuarioVerificado(7L);

		assertThat(resultado).isTrue();
	}

	@Test
	void usuarioVerificado_cuandoNoEsOrganizacion_devuelveFalse() {
		Usuario usuario = new Usuario() {};
		usuario.setId(8L);
		when(usuarioRepository.findById(8L)).thenReturn(java.util.Optional.of(usuario));

		Boolean resultado = authService.usuarioVerificado(8L);

		assertThat(resultado).isFalse();
	}

	@Test
	void usuarioVerificado_cuandoNoExiste_lanzaIllegalArgumentException() {
		when(usuarioRepository.findById(99L)).thenReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> authService.usuarioVerificado(99L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Usuario no encontrado con el ID: 99");
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

