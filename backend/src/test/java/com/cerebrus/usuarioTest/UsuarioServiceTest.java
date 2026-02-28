package com.cerebrus.usuarioTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cerebrus.exceptions.ResourceNotFoundException;
import com.cerebrus.organizacion.Organizacion;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

    // Captor para capturar el usuario que se guarda en el repositorio
	@Captor
	private ArgumentCaptor<Usuario> usuarioCaptor;

    // Método para limpiar el SecurityContext después de cada test
	@AfterEach
	void limpiarSecurityContext() {
		SecurityContextHolder.clearContext();
	}

    // Test para verificar que el método saveUser codifica la contraseña cuando se proporciona una contraseña no vacía
	@Test
	void saveUser_codificaContrasenaOk() {
		Usuario user = crearAlumno("user1", "user1@cerebrus.com", "plain-pass");
		when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		Usuario saved = usuarioService.saveUser(user);

		assertThat(saved.getContrasena()).isEqualTo("encoded-pass");
		verify(passwordEncoder).encode("plain-pass");
		verify(usuarioRepository).save(usuarioCaptor.capture());
		assertThat(usuarioCaptor.getValue().getContrasena()).isEqualTo("encoded-pass");
	}

    // Test para verificar que el método saveUser no codifica la contraseña cuando se proporciona una contraseña 
    // en blanco
	@Test
	void saveUser_noCodificaContrasena_cuandoEsBlanco() {
		Usuario userBlank = crearAlumno("user2", "user2@cerebrus.com", "   ");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		Usuario savedBlank = usuarioService.saveUser(userBlank);

		assertThat(savedBlank.getContrasena()).isEqualTo("   ");
		verify(passwordEncoder, never()).encode(any());
		verify(usuarioRepository).save(any(Usuario.class));
	}

    // Test para verificar que el método saveUser no codifica la contraseña cuando se proporciona un string vacío
	@Test
	void saveUser_noCodificaContrasena_cuandoEsVacio() {
		Usuario userEmpty = crearAlumno("userEmpty", "empty@cerebrus.com", "");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		Usuario saved = usuarioService.saveUser(userEmpty);

		assertThat(saved.getContrasena()).isEqualTo("");
		verify(passwordEncoder, never()).encode(any());
		verify(usuarioRepository).save(any(Usuario.class));
	}

    // Test para verificar que el método saveUser no codifica la contraseña cuando se proporciona una contraseña nula
	@Test
	void saveUser_noCodificaContrasena_cuandoEsNull() {
		Usuario userNull = crearAlumno("userNull", "null@cerebrus.com", "will-be-null");
		userNull.setContrasena(null);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		Usuario saved = usuarioService.saveUser(userNull);

		assertThat(saved.getContrasena()).isNull();
		verify(passwordEncoder, never()).encode(any());
		verify(usuarioRepository).save(any(Usuario.class));
	}

    // Test para verificar que el método findByUsername devuelve el usuario encontrado por username o email
	@Test
	void findByUsername_devuelveUsuario_encontradoPorEmailUsername() {
		Usuario expected = crearAlumno("userA", "a@cerebrus.com", "x");
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico("userA", "a@cerebrus.com"))
				.thenReturn(Optional.of(expected));

		Usuario found = usuarioService.findByUsername("userA", "a@cerebrus.com");

		assertThat(found).isSameAs(expected);
		verify(usuarioRepository).findByNombreUsuarioOrCorreoElectronico("userA", "a@cerebrus.com");
	}

    // Test para verificar que el método findByUsername lanza una excepción si no se encuentra el usuario por 
    // username ni email
	@Test
	void findByUsername_lanzaResourceNotFound_cuandoNoExiste() {
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico(eq("missing"), eq("missing@cerebrus.com")))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService.findByUsername("missing", "missing@cerebrus.com"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Usuario not found");
	}

    // Test para verificar que el método findById devuelve el usuario encontrado por id
	@Test
	void findById_devuelveUsuarioOk() {
		Usuario expected = crearAlumno("user3", "user3@cerebrus.com", "x");
		expected.setId(10L);
		when(usuarioRepository.findById(10L)).thenReturn(Optional.of(expected));

		Usuario found = usuarioService.findById(10L);

		assertThat(found).isSameAs(expected);
	}

    // Test para verificar que el método findById lanza una excepción si no se encuentra el usuario por id
	@Test
	void findById_lanzaResourceNotFound_cuandoNoExiste() {
		when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService.findById(404L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Usuario not found")
				.hasMessageContaining("id");
	}

    // Test para verificar que el método findCurrentUser devuelve el usuario autenticado
	@Test
	void findCurrentUser_devuelveUsuarioAutenticado() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("currentUser", "N/A"));
		Usuario expected = crearAlumno("currentUser", "current@cerebrus.com", "x");
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico("currentUser", "currentUser"))
				.thenReturn(Optional.of(expected));

		Usuario found = usuarioService.findCurrentUser();

		assertThat(found).isSameAs(expected);
		verify(usuarioRepository).findByNombreUsuarioOrCorreoElectronico("currentUser", "currentUser");
	}

    // Test para verificar que el método findCurrentUser lanza una excepción si no hay ningún usuario autenticado
	@Test
	void findCurrentUser_lanzaResourceNotFound_cuandoNoHayUsuarioAutenticado() {
		SecurityContextHolder.clearContext();

		assertThatThrownBy(() -> usuarioService.findCurrentUser())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Nobody authenticated");
	}

    // Test para verificar que el método findCurrentUser lanza una excepción si el usuario autenticado no se 
    // encuentra en la base de datos
	@Test
	void findCurrentUser_lanzaResourceNotFound_cuandoUsuarioAutenticadoNoExiste() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("currentUser", "N/A"));
		when(usuarioRepository.findByNombreUsuarioOrCorreoElectronico("currentUser", "currentUser"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService.findCurrentUser())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Usuario not found")
				.hasMessageContaining("NombreUsuario");
	}

    // Test para verificar que el método existsUser delega correctamente en el repositorio y devuelve el resultado
	@Test
	void existsUserOk() {
		when(usuarioRepository.existsByNombreUsuario("pepe")).thenReturn(true);

		Boolean exists = usuarioService.existsUser("pepe");

		assertThat(exists).isTrue();
		verify(usuarioRepository).existsByNombreUsuario("pepe");
	}

    // Test para verificar que el método existsUser devuelve false cuando el repositorio indica que el usuario no
    // existe
	@Test
	void existsUser_devuelveFalse_siNoExiste() {
		when(usuarioRepository.existsByNombreUsuario("missing")).thenReturn(false);

		Boolean exists = usuarioService.existsUser("missing");

		assertThat(exists).isFalse();
		verify(usuarioRepository).existsByNombreUsuario("missing");
	}

    // Test para verificar que el método findAll delega correctamente en el repositorio y devuelve el resultado
	@Test
	void findAllOk() {
		when(usuarioRepository.findAll()).thenReturn(List.of());

		Iterable<Usuario> all = usuarioService.findAll();

		assertThat(all).isNotNull();
		verify(usuarioRepository).findAll();
	}

    // Test para verificar que el método updateUser actualiza correctamente un usuario existente y codifica la 
    // contraseña si se proporciona una nueva
	@Test
	void updateUser_codificaYActualizaContrasena_Ok() {
		Alumno existing = crearAlumno("oldUser", "old@cerebrus.com", "oldEncoded");
		existing.setId(5L);
		when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existing));
		when(passwordEncoder.encode("new-pass")).thenReturn("newEncoded");

		Alumno incoming = crearAlumno("newUser", "new@cerebrus.com", "new-pass");
		incoming.setNombre("NuevoNombre");
		incoming.setPrimerApellido("NuevoApellido1");
		incoming.setSegundoApellido("NuevoApellido2");

		Usuario updated = usuarioService.updateUser(incoming, 5L);

		assertThat(updated.getId()).isEqualTo(5L);
		assertThat(updated.getNombreUsuario()).isEqualTo("newUser");
		assertThat(updated.getCorreoElectronico()).isEqualTo("new@cerebrus.com");
		assertThat(updated.getNombre()).isEqualTo("NuevoNombre");
		assertThat(updated.getPrimerApellido()).isEqualTo("NuevoApellido1");
		assertThat(updated.getSegundoApellido()).isEqualTo("NuevoApellido2");
		assertThat(updated.getContrasena()).isEqualTo("newEncoded");
		verify(passwordEncoder).encode("new-pass");
		verify(usuarioRepository).save(existing);
	}

    // Test para verificar que el método updateUser no actualiza la contraseña si se proporciona una contraseña en blanco
	@Test
	void updateUser_noActualizaContrasenaCuandoEstaVacia() {
		Alumno existing = crearAlumno("oldUser", "old@cerebrus.com", "oldEncoded");
		existing.setId(6L);
		when(usuarioRepository.findById(6L)).thenReturn(Optional.of(existing));

		Alumno incoming = crearAlumno("updatedUser", "updated@cerebrus.com", "   ");
		incoming.setNombre("NombreActualizado");
		incoming.setPrimerApellido("Apellido1");
		incoming.setSegundoApellido("Apellido2");

		Usuario updated = usuarioService.updateUser(incoming, 6L);

		assertThat(updated.getId()).isEqualTo(6L);
		assertThat(updated.getNombreUsuario()).isEqualTo("updatedUser");
		assertThat(updated.getCorreoElectronico()).isEqualTo("updated@cerebrus.com");
		assertThat(updated.getContrasena()).isEqualTo("oldEncoded");
		verify(passwordEncoder, never()).encode(any());
		verify(usuarioRepository).save(existing);
	}

    // Test para verificar que el método updateUser no actualiza la contraseña si se proporciona una contraseña nula
	@Test
	void updateUser_noActualizaContrasenaCuandoEsNull() {
		Alumno existing = crearAlumno("oldUser", "old@cerebrus.com", "oldEncoded");
		existing.setId(7L);
		when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existing));

		Alumno incoming = crearAlumno("updatedUser", "updated@cerebrus.com", "will-be-null");
		incoming.setContrasena(null);
		incoming.setNombre("NombreActualizado");
		incoming.setPrimerApellido("Apellido1");
		incoming.setSegundoApellido("Apellido2");

		Usuario updated = usuarioService.updateUser(incoming, 7L);

		assertThat(updated.getId()).isEqualTo(7L);
		assertThat(updated.getNombreUsuario()).isEqualTo("updatedUser");
		assertThat(updated.getCorreoElectronico()).isEqualTo("updated@cerebrus.com");
		assertThat(updated.getContrasena()).isEqualTo("oldEncoded");
		verify(passwordEncoder, never()).encode(any());
		verify(usuarioRepository).save(existing);
	}

    // Test para verificar que el método updateUser lanza una excepción si el usuario a actualizar no existe
	@Test
	void updateUser_lanzaResourceNotFound_cuandoIdNoExiste() {
		when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

		Alumno incoming = crearAlumno("u", "u@cerebrus.com", "new-pass");

		assertThatThrownBy(() -> usuarioService.updateUser(incoming, 999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Usuario not found")
				.hasMessageContaining("id");
		verify(usuarioRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

    // Test para verificar que el método deleteUser elimina correctamente un usuario existente
	@Test
	void deleteUserOk() {
		Usuario existing = crearAlumno("del", "del@cerebrus.com", "x");
		existing.setId(99L);
		when(usuarioRepository.findById(99L)).thenReturn(Optional.of(existing));

		usuarioService.deleteUser(99L);

		verify(usuarioRepository).delete(existing);
	}

    // Test para verificar que el método deleteUser lanza una excepción si el usuario a eliminar no existe
	@Test
	void deleteUser_lanzaResourceNotFound_cuandoNoExiste() {
		when(usuarioRepository.findById(1234L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService.deleteUser(1234L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Usuario not found")
				.hasMessageContaining("id");
		verify(usuarioRepository, never()).delete(any());
	}

    // Método auxiliar para crear un alumno de prueba
	private static Alumno crearAlumno(String username, String email, String password) {
		return new Alumno(
				"Ana",
				"Pérez",
				"García",
				username,
				email,
				password,
				0,
				new Organizacion("Org"));
	}
}
