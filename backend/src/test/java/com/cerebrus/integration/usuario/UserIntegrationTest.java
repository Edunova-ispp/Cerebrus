package com.cerebrus.integration.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.auth.AuthController;
import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.payload.request.LoginRequest;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.auth.security.JwtUtils;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.suscripcion.SuscripcionRepository;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.MaestroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserIntegrationTest {

    private MockMvc mockMvc;

    @Mock private AuthService authService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private AlumnoRepository alumnoRepository;
    @Mock private MaestroRepository maestroRepository;
    @Mock private SuscripcionRepository suscripcionRepository;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegistroCorrecto() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setNombre("Juan");
        signupRequest.setPrimerApellido("Integracion");
        signupRequest.setSegundoApellido("Test");
        signupRequest.setUsername("juan_int_test");
        signupRequest.setEmail("juan_test@cerebrus.com");
        signupRequest.setPassword("password123");
        signupRequest.setTipoUsuario("ALUMNO");

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(false);
        doNothing().when(authService).registrarUsuario(any(SignupRequest.class));

        // Ruta real: /auth/register
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("¡Usuario registrado correctamente!"));
    }

    @Test
void testInicioDeSesionCorrecto() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setIdentificador("juan_int_test");
    loginRequest.setPassword("password123");

    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ADMIN"));
    UserDetailsImpl principal = new UserDetailsImpl(1L, "juan_int_test", "pass", true, authorities);

    Authentication authentication = new org.springframework.security.authentication
            .UsernamePasswordAuthenticationToken(principal, null, authorities);

    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("token-fake");

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-fake"))
            .andExpect(jsonPath("$.username").value("juan_int_test"));
}

    @Test
    void testLoginDatosIncorrectos_DebeDevolverUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentificador("usuario_fantasma");
        loginRequest.setPassword("clave_incorrecta");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        // Ruta real: /auth/login
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void testCerrarSesion_DebeLimpiarSesion() throws Exception {
        // Ruta real: /auth/logout
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Sesión cerrada. El cliente debe eliminar el token."));
    }

    @Test
    void testRegistroUsernameYaEnUso_DebeDevolver400() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setNombre("Juan");
        signupRequest.setPrimerApellido("Dup");
        signupRequest.setSegundoApellido("Test");
        signupRequest.setUsername("juan_duplicado");
        signupRequest.setEmail("nuevo@cerebrus.com");
        signupRequest.setPassword("password123");
        signupRequest.setTipoUsuario("ALUMNO");

        when(authService.existsByUsername("juan_duplicado")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: El nombre de usuario ya está en uso."));
    }

    @Test
void testLoginAlumnoSinSuscripcion_DebeDevolver403() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setIdentificador("alumno_sin_org");
    loginRequest.setPassword("password123");

    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ALUMNO"));
    UserDetailsImpl principal = new UserDetailsImpl(2L, "alumno_sin_org", "pass", true, authorities);

    Authentication authentication = new org.springframework.security.authentication
            .UsernamePasswordAuthenticationToken(principal, null, authorities);

    when(authenticationManager.authenticate(any())).thenReturn(authentication);

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("CUENTA_ORG_NO_SUSCRIPCION"));
}
}