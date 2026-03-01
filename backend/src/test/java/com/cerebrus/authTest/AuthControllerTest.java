package com.cerebrus.authTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cerebrus.auth.AuthController;
import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.payload.request.LoginRequest;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.auth.payload.response.JwtResponse;
import com.cerebrus.auth.payload.response.MessageResponse;
import com.cerebrus.auth.security.JwtUtils;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private AuthService authService;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private AuthController authController;

    // Captor para capturar el token de autenticación usado en el login
	@Captor
	private ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor;

    // Método para limpiar el SecurityContext después de cada test
	@AfterEach
	void limpiarSecurityContext() {
		SecurityContextHolder.clearContext();
	}

    // Test para verificar que el registro de usuario devuelve un error si el username ya existe
	@Test
	void registerUser_cuandoUsernameYaExiste_devuelveBadRequest() {
		SignupRequest request = crearSignupRequest();
		when(authService.existsByUsername(request.getUsername())).thenReturn(true);

		ResponseEntity<?> response = authController.registerUser(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage())
				.isEqualTo("Error: El nombre de usuario ya está en uso.");
		verify(authService).existsByUsername(request.getUsername());
		verify(authService, never()).existsByEmail(any());
		verify(authService, never()).registrarUsuario(any());
	}

    // Test para verificar que el registro de usuario devuelve un error si el email ya existe
	@Test
	void registerUser_cuandoEmailYaExiste_devuelveBadRequest() {
		SignupRequest request = crearSignupRequest();
		when(authService.existsByUsername(request.getUsername())).thenReturn(false);
		when(authService.existsByEmail(request.getEmail())).thenReturn(true);

		ResponseEntity<?> response = authController.registerUser(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage())
				.isEqualTo("Error: El correo electrónico ya está en uso.");
		verify(authService).existsByUsername(request.getUsername());
		verify(authService).existsByEmail(request.getEmail());
		verify(authService, never()).registrarUsuario(any());
	}

    // Test para verificar que el login funciona correctamente y maneja los casos de error
	@Test
	void registerUser_cuandoRegistroOk_devuelveCreated() {
		SignupRequest request = crearSignupRequest();
		when(authService.existsByUsername(request.getUsername())).thenReturn(false);
		when(authService.existsByEmail(request.getEmail())).thenReturn(false);

		ResponseEntity<?> response = authController.registerUser(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage())
				.isEqualTo("¡Usuario registrado correctamente!");
		verify(authService).registrarUsuario(request);
	}

    // Test para verificar que el registro de usuario devuelve un error si el servicio lanza una excepción 
    // (por ejemplo, por un tipo de usuario inválido)
	@Test
	void registerUser_cuandoRegistroLanzaIllegalArgument_devuelveBadRequestConMensaje() {
		SignupRequest request = crearSignupRequest();
		when(authService.existsByUsername(request.getUsername())).thenReturn(false);
		when(authService.existsByEmail(request.getEmail())).thenReturn(false);
		doThrow(new IllegalArgumentException("Tipo inválido")).when(authService).registrarUsuario(request);

		ResponseEntity<?> response = authController.registerUser(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage()).isEqualTo("Tipo inválido");
	}

    // Test para verificar que el login funciona correctamente y maneja los casos de error
	@Test
	void authenticateUser_cuandoCredencialesOk_devuelveJwtResponseYSeteaSecurityContext() {
		LoginRequest request = new LoginRequest();
		request.setIdentificador("alumno1");
		request.setPassword("pass");

		Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
		UserDetailsImpl principal = new UserDetailsImpl(7L, "alumno1", "pass",
				List.of(new SimpleGrantedAuthority("ALUMNO")));

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(principal);
		when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

		ResponseEntity<?> response = authController.authenticateUser(request);

		verify(authenticationManager).authenticate(authTokenCaptor.capture());
		UsernamePasswordAuthenticationToken usedToken = authTokenCaptor.getValue();
		assertThat(usedToken.getPrincipal()).isEqualTo("alumno1");
		assertThat(usedToken.getCredentials()).isEqualTo("pass");

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(JwtResponse.class);

		JwtResponse body = (JwtResponse) response.getBody();
		assertThat(body.getToken()).isEqualTo("jwt-token");
		assertThat(body.getId()).isEqualTo(7L);
		assertThat(body.getUsername()).isEqualTo("alumno1");
		assertThat(body.getRoles()).containsExactly("ALUMNO");
		assertThat(body.getType()).isEqualTo("Bearer");
	}

    // Test para verificar que el login devuelve un error si las credenciales son incorrectas
	@Test
	void authenticateUser_cuandoBadCredentials_devuelveUnauthorized() {
		LoginRequest request = new LoginRequest();
		request.setIdentificador("x");
		request.setPassword("y");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("bad"));

		ResponseEntity<?> response = authController.authenticateUser(request);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage())
				.isEqualTo("Credenciales incorrectas");
	}

    // Test para verificar que el logout funciona correctamente y limpia el SecurityContext
	@Test
	void logoutUser_limpiaSecurityContextYDevuelveOk() {
		SecurityContextHolder.getContext().setAuthentication(org.mockito.Mockito.mock(Authentication.class));

		ResponseEntity<?> response = authController.logoutUser();

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
		assertThat(((MessageResponse) response.getBody()).getMessage())
				.isEqualTo("Sesión cerrada. El cliente debe eliminar el token.");
	}

    // Método auxiliar para crear un SignupRequest de prueba
	private static SignupRequest crearSignupRequest() {
		SignupRequest request = new SignupRequest();
		request.setNombre("Ana");
		request.setPrimerApellido("Pérez");
		request.setSegundoApellido("García");
		request.setUsername("anaperez");
		request.setEmail("ana@cerebrus.com");
		request.setPassword("plain-pass");
		request.setTipoUsuario("ALUMNO");
		return request;
	}
}

