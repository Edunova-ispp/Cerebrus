package com.cerebrus.authTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Director;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;

class UserDetailsImplTest {

    // Test para verificar que el método build asigna correctamente el rol de Alumno cuando el usuario 
    // es una instancia de Alumno
    @Test
    void build_cuandoUsuarioEsAlumno_asignaRolAlumno() {
        Alumno alumno = new Alumno();
        alumno.setId(1L);
        alumno.setNombreUsuario("alumno1");
        alumno.setContrasena("pass");

        UserDetailsImpl userDetails = UserDetailsImpl.build(alumno);

        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("alumno1");
        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(roles(userDetails)).containsExactly("ROLE_ALUMNO");
    }

    // Test para verificar que el método build asigna correctamente el rol de Maestro cuando el usuario 
    // es una instancia de Maestro
    @Test
    void build_cuandoUsuarioEsMaestro_asignaRolMaestro() {
        Maestro maestro = new Maestro();
        maestro.setId(2L);
        maestro.setNombreUsuario("maestro1");
        maestro.setContrasena("pass");

        UserDetailsImpl userDetails = UserDetailsImpl.build(maestro);

        assertThat(userDetails.getId()).isEqualTo(2L);
        assertThat(userDetails.getUsername()).isEqualTo("maestro1");
        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(roles(userDetails)).containsExactly("ROLE_MAESTRO");
    }

    // Test para verificar que el método build asigna correctamente el rol de Director cuando el usuario 
    // es una instancia de Director
    @Test
    void build_cuandoUsuarioEsDirector_asignaRolDirector() {
        Director director = new Director();
        director.setId(3L);
        director.setNombreUsuario("director1");
        director.setContrasena("pass");

        UserDetailsImpl userDetails = UserDetailsImpl.build(director);

        assertThat(userDetails.getId()).isEqualTo(3L);
        assertThat(userDetails.getUsername()).isEqualTo("director1");
        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(roles(userDetails)).containsExactly("ROLE_DIRECTOR");
    }

    // Test para verificar que el método build asigna un rol genérico de "USUARIO" si el usuario no es 
    // una instancia de Alumno, Maestro o Director
    @Test
    void build_cuandoUsuarioNoEsSubtipoConocido_asignaRolUsuario() {
        Usuario usuario = new Usuario() {
        };
        usuario.setId(4L);
        usuario.setNombreUsuario("usuario1");
        usuario.setContrasena("pass");

        UserDetailsImpl userDetails = UserDetailsImpl.build(usuario);

        assertThat(userDetails.getId()).isEqualTo(4L);
        assertThat(userDetails.getUsername()).isEqualTo("usuario1");
        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(roles(userDetails)).containsExactly("ROLE_USUARIO");
    }

    // Verificar que equals y hashCode se basan solo en el id
    @Test
    void equals_yHashCode_seBasenEnId() {
        UserDetailsImpl a = new UserDetailsImpl(10L, "u1", "p1", List.of());
        UserDetailsImpl b = new UserDetailsImpl(10L, "u2", "p2", List.of());
        UserDetailsImpl c = new UserDetailsImpl(11L, "u1", "p1", List.of());

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }

    // Test para verificar que cuando el user es null o de distinta clase, el método equals devuelve false
    @Test
    void equals_cuandoEsNullODistintaClase_devuelveFalse() {
        UserDetailsImpl a = new UserDetailsImpl(10L, "u1", "p1", List.of());
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("otro")).isFalse();
    }

    // Test para verificar que los métodos de flags de cuenta devuelven true
    @Test
    void flagsDeCuenta_sonTrue() {
        UserDetailsImpl a = new UserDetailsImpl(10L, "u1", "p1", List.of());
        assertThat(a.isAccountNonExpired()).isTrue();
        assertThat(a.isAccountNonLocked()).isTrue();
        assertThat(a.isCredentialsNonExpired()).isTrue();
        assertThat(a.isEnabled()).isTrue();
    }

    // Método auxiliar para extraer los nombres de los roles de las autoridades
    private static List<String> roles(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
