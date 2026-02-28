package com.cerebrus.authTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.UserDetailsServiceImpl;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

    // Test para verificar que se carga correctamente un usuario existente
	@Test
	void loadUserByUsernameTestCorrecto() {
		Alumno alumno = new Alumno();
		alumno.setId(1L);
		alumno.setNombreUsuario("alumno1");
		alumno.setContrasena("pass");

		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico("alumno1", "alumno1"))
				.thenReturn(Optional.of(alumno));

		UserDetails result = userDetailsService.loadUserByUsername("alumno1");

		verify(usuarioRepository).findByNombreUsuarioOrCorreoElectronico("alumno1", "alumno1");
		assertThat(result).isInstanceOf(UserDetailsImpl.class);
		assertThat(result.getUsername()).isEqualTo("alumno1");
		assertThat(result.getPassword()).isEqualTo("pass");
		assertThat(roles(result)).containsExactly("ROLE_ALUMNO");
	}

    // Test para verificar que se lanza una excepción al intentar cargar un usuario inexistente
	@Test
	void loadUserByUsernameTestIncorrecto() {
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico("noexiste", "noexiste"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("noexiste"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Usuario no encontrado");

		verify(usuarioRepository).findByNombreUsuarioOrCorreoElectronico("noexiste", "noexiste");
	}

    // Test para verificar que se puede cargar un usuario usando su correo electrónico en lugar de su nombre de usuario
	@Test
	void loadUserByUsername_conEmail_funcionaIgualQueConUsername() {
		Alumno alumno = new Alumno();
		alumno.setId(2L);
		alumno.setNombreUsuario("alumno2");
		alumno.setContrasena("pass");

		String email = "alumno2@cerebrus.com";
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico(email, email)).thenReturn(Optional.of(alumno));

		UserDetails result = userDetailsService.loadUserByUsername(email);

		verify(usuarioRepository).findByNombreUsuarioOrCorreoElectronico(email, email);
		assertThat(result.getUsername()).isEqualTo("alumno2");
		assertThat(roles(result)).containsExactly("ROLE_ALUMNO");
	}

    // Método auxiliar para extraer los roles de un UserDetails
	private static List<String> roles(UserDetails userDetails) {
		return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
	}
}

