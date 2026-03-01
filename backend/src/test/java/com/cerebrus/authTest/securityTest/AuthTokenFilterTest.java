package com.cerebrus.authTest.securityTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.UserDetailsServiceImpl;
import com.cerebrus.auth.security.AuthTokenFilter;
import com.cerebrus.auth.security.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserDetailsServiceImpl userDetailsService;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private AuthTokenFilter authTokenFilter;

    // Método para limpiar el SecurityContext después de cada test
	@AfterEach
	void limpiarSecurityContext() {
		SecurityContextHolder.clearContext();
	}

    // Test para verificar que si no hay header Authorization, no se autentica y se continúa la cadena de filtros
	@Test
	void doFilter_sinHeaderAuthorization_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils, never()).validateJwtToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

    // Test para verificar que si el header Authorization no empieza con "Bearer ", no se autentica y se continúa 
    // la cadena de filtros
	@Test
	void doFilter_headerNoBearer_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Basic abcdef");
		MockHttpServletResponse response = new MockHttpServletResponse();

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils, never()).validateJwtToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

    // Test para verificar que si el token es válido, se autentica correctamente y se continúa la cadena de filtros
	@Test
	void doFilter_tokenValido_autenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token-valido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("token-valido")).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken("token-valido")).thenReturn("alumno1");

		UserDetails userDetails = new UserDetailsImpl(
				7L,
				"alumno1",
				"pass",
				List.of(new SimpleGrantedAuthority("ALUMNO")));
		when(userDetailsService.loadUserByUsername("alumno1")).thenReturn(userDetails);

		authTokenFilter.doFilter(request, response, filterChain);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getPrincipal()).isSameAs(userDetails);
		assertThat(auth.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ALUMNO");

		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("token-valido");
		verify(jwtUtils).getUserNameFromJwtToken("token-valido");
		verify(userDetailsService).loadUserByUsername("alumno1");
	}

    // Test para verificar que si el token es inválido, no se autentica y se continúa la cadena de filtros
	@Test
	void doFilter_tokenInvalido_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token-invalido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("token-invalido")).thenReturn(false);

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("token-invalido");
		verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

    // Test para verificar que si el token es un Bearer pero no tiene token (ej: "Bearer "), no se autentica y 
    // se continúa la cadena de filtros 
	@Test
	void doFilter_bearerSinToken_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer ");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("")).thenReturn(false);

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("");
		verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

    // Test para verificar que si ocurre una excepción al extraer el username del token, no se autentica y 
    // se continúa la cadena de filtros
	@Test
	void doFilter_siFallaExtraerUsername_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token-valido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("token-valido")).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken("token-valido")).thenThrow(new RuntimeException("boom"));

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("token-valido");
		verify(jwtUtils).getUserNameFromJwtToken("token-valido");
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

    // Test para verificar que si ocurre una excepción al cargar el usuario, no se autentica y se continúa 
    // la cadena de filtros
	@Test
	void doFilter_siFallaCargarUsuario_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token-valido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("token-valido")).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken("token-valido")).thenReturn("alumno1");
		when(userDetailsService.loadUserByUsername("alumno1")).thenThrow(new RuntimeException("boom"));

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("token-valido");
		verify(jwtUtils).getUserNameFromJwtToken("token-valido");
		verify(userDetailsService).loadUserByUsername("alumno1");
	}

    // Test para verificar que si ocurre una excepción al validar el token, no se autentica y se continúa 
    // la cadena de filtros
	@Test
	void doFilter_siOcurreExcepcion_noAutenticaYContinuaCadena() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("token")).thenThrow(new RuntimeException("boom"));

		authTokenFilter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken("token");
		verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}
}

