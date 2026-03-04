package com.cerebrus.authTest.securityTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.security.JwtUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    // Clave secreta de prueba con longitud suficiente para HS512
    private static final String SECRET_LARGO_HS512 =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef" +
            "0123456789abcdef0123456789abcdef";

    @Mock
    private Authentication authentication;

    // Test para verificar que se genera un token JWT válido, se valida correctamente y se puede extraer el username
    @Test
    void generateJwtToken_yValidateJwtToken_yGetUserNameFromJwtToken_funcionanConTokenValido() {
        JwtUtils jwtUtils = new JwtUtils();
        inyectarCampo(jwtUtils, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtils, "jwtExpirationMs", 60_000);

        UserDetailsImpl principal = new UserDetailsImpl(
                7L,
                "alumno1",
                "pass",
                List.of(new SimpleGrantedAuthority("ALUMNO")));
        when(authentication.getPrincipal()).thenReturn(principal);

        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("alumno1");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_LARGO_HS512.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        assertThat(claims.getSubject()).isEqualTo("alumno1");
        assertThat(((Number) claims.get("id")).longValue()).isEqualTo(7L);

        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");
        assertThat(authorities).containsExactly("ALUMNO");
    }

    // Test para verificar que un token malformado no es válido
    @Test
    void validateJwtToken_cuandoTokenMalformado_devuelveFalse() {
        JwtUtils jwtUtils = new JwtUtils();
        inyectarCampo(jwtUtils, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtils, "jwtExpirationMs", 60_000);

        assertThat(jwtUtils.validateJwtToken("no-es-un-jwt")).isFalse();
    }

    // Test para verificar que un token es null no es válido
    @Test
    void validateJwtToken_cuandoTokenEsNull_devuelveFalse() {
        JwtUtils jwtUtils = new JwtUtils();
        inyectarCampo(jwtUtils, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtils, "jwtExpirationMs", 60_000);

        assertThat(jwtUtils.validateJwtToken(null)).isFalse();
    }

    // Test para verificar que un token con algoritmo "none" no es válido
    @Test
    void validateJwtToken_cuandoTokenSinFirma_algNone_devuelveFalse() {
        JwtUtils jwtUtils = new JwtUtils();
        inyectarCampo(jwtUtils, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtils, "jwtExpirationMs", 60_000);

        // Header {"alg":"none"} + payload {"sub":"usuario"} con firma vacía.
        String tokenSinFirma = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c3VhcmlvIn0.";
        assertThat(jwtUtils.validateJwtToken(tokenSinFirma)).isFalse();
    }

    // Test para verificar que un token con firma inválida devuelve false
    @Test
    void validateJwtToken_cuandoFirmaInvalida_devuelveFalse() {
        JwtUtils jwtUtilsValidador = new JwtUtils();
        inyectarCampo(jwtUtilsValidador, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtilsValidador, "jwtExpirationMs", 60_000);

        JwtUtils jwtUtilsFirmador = new JwtUtils();
        inyectarCampo(jwtUtilsFirmador, "jwtSecret", SECRET_LARGO_HS512 + "cambio");
        inyectarCampo(jwtUtilsFirmador, "jwtExpirationMs", 60_000);

        UserDetailsImpl principal = new UserDetailsImpl(
                1L,
                "usuario1",
                "pass",
                List.of(new SimpleGrantedAuthority("USUARIO")));
        when(authentication.getPrincipal()).thenReturn(principal);

        String tokenConOtraFirma = jwtUtilsFirmador.generateJwtToken(authentication);

        assertThat(jwtUtilsValidador.validateJwtToken(tokenConOtraFirma)).isFalse();
    }

    // Test para verificar que un token expirado no es válido
    @Test
    void validateJwtToken_cuandoTokenExpirado_devuelveFalse() {
        JwtUtils jwtUtils = new JwtUtils();
        inyectarCampo(jwtUtils, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtils, "jwtExpirationMs", -1_000);

        UserDetailsImpl principal = new UserDetailsImpl(
                1L,
                "usuario1",
                "pass",
                List.of(new SimpleGrantedAuthority("USUARIO")));
        when(authentication.getPrincipal()).thenReturn(principal);

        String tokenExpirado = jwtUtils.generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(tokenExpirado)).isFalse();
    }

    // Test para verificar que intentar extraer el username de un token con firma inválida lanza una excepción
    @Test
    void getUserNameFromJwtToken_cuandoFirmaInvalida_lanzaExcepcion() {
        JwtUtils jwtUtilsValidador = new JwtUtils();
        inyectarCampo(jwtUtilsValidador, "jwtSecret", SECRET_LARGO_HS512);
        inyectarCampo(jwtUtilsValidador, "jwtExpirationMs", 60_000);

        JwtUtils jwtUtilsFirmador = new JwtUtils();
        inyectarCampo(jwtUtilsFirmador, "jwtSecret", SECRET_LARGO_HS512 + "cambio");
        inyectarCampo(jwtUtilsFirmador, "jwtExpirationMs", 60_000);

        UserDetailsImpl principal = new UserDetailsImpl(
                1L,
                "usuario1",
                "pass",
                List.of(new SimpleGrantedAuthority("USUARIO")));
        when(authentication.getPrincipal()).thenReturn(principal);

        String tokenConOtraFirma = jwtUtilsFirmador.generateJwtToken(authentication);

        assertThatThrownBy(() -> jwtUtilsValidador.getUserNameFromJwtToken(tokenConOtraFirma))
                .isInstanceOfAny(RuntimeException.class);
    }

    // Método auxiliar para inyectar valores en campos privados de JwtUtils
    private static void inyectarCampo(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No se pudo inyectar el campo: " + fieldName, e);
        }
    }
}
