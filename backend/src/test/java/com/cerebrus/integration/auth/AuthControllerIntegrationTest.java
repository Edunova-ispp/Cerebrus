package com.cerebrus.integration.auth;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cerebrus.auth.AuthController;
import com.cerebrus.auth.AuthService;
import com.cerebrus.auth.UserDetailsImpl;
import com.cerebrus.auth.payload.request.LoginRequest;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.auth.payload.response.JwtResponse;
import com.cerebrus.auth.payload.response.MessageResponse;
import com.cerebrus.auth.security.JwtUtils;
import com.cerebrus.exceptions.GlobalExceptionHandler;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.suscripcion.SuscripcionRepository;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.maestro.MaestroRepository;
import com.cerebrus.usuario.organizacion.Organizacion;

@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthService authService;

    @Mock
    private MaestroRepository maestroRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void limpiarSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUser_usernameExiste_devuelve400() throws Exception {
        when(authService.existsByUsername("anaperez")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSignupJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: El nombre de usuario ya está en uso."));
    }

    @Test
    void registerUser_emailExiste_devuelve400() throws Exception {
        when(authService.existsByUsername("anaperez")).thenReturn(false);
        when(authService.existsByEmail("ana@cerebrus.com")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSignupJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: El correo electrónico ya está en uso."));
    }

    @Test
    void registerUser_ok_devuelve201() throws Exception {
        when(authService.existsByUsername("anaperez")).thenReturn(false);
        when(authService.existsByEmail("ana@cerebrus.com")).thenReturn(false);
        doNothing().when(authService).registrarUsuario(any(SignupRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSignupJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("¡Usuario registrado correctamente!"));
    }

    @Test
    void registerUser_illegalArgument_devuelve400() throws Exception {
        when(authService.existsByUsername("anaperez")).thenReturn(false);
        when(authService.existsByEmail("ana@cerebrus.com")).thenReturn(false);
        doThrow(new IllegalArgumentException("Tipo inválido")).when(authService).registrarUsuario(any(SignupRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSignupJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo inválido"));
    }

    @Test
    void authenticateUser_conMaestroSinSuscripcion_devuelve403() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentificador("maestro1");
        loginRequest.setPassword("pass");

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserDetailsImpl principal = new UserDetailsImpl(5L, "maestro1", "pass", true,
                List.of(new SimpleGrantedAuthority("MAESTRO")));

        Organizacion org = crearOrganizacion(100L);
        Maestro maestro = crearMaestro(5L, org);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(maestroRepository.findById(5L)).thenReturn(Optional.of(maestro));
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(100L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearLoginJson("maestro1", "pass")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CUENTA_ORG_NO_SUSCRIPCION"));
    }

    @Test
    void authenticateUser_conAlumnoYSuscripcionActiva_devuelveJwtResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentificador("alumno1");
        loginRequest.setPassword("pass");

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserDetailsImpl principal = new UserDetailsImpl(7L, "alumno1", "pass", true,
                List.of(new SimpleGrantedAuthority("ALUMNO")));

        Organizacion org = crearOrganizacion(101L);
        Alumno alumno = crearAlumno(7L, org);
        Suscripcion suscripcion = new Suscripcion();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(alumnoRepository.findById(7L)).thenReturn(Optional.of(alumno));
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(101L)).thenReturn(Optional.of(suscripcion));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearLoginJson("alumno1", "pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("alumno1"))
                .andExpect(jsonPath("$.roles[0]").value("ALUMNO"));
    }

    @Test
    void authenticateUser_disabled_devuelve403() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("disabled"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearLoginJson("x", "y")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CUENTA_NO_VERIFICADA"));
    }

    @Test
    void authenticateUser_badCredentials_devuelve401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearLoginJson("x", "y")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void logoutUser_devuelveOkYLimpaContexto() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(org.mockito.Mockito.mock(Authentication.class));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada. El cliente debe eliminar el token."));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void confirmarEmail_ok_devuelve200() throws Exception {
        doNothing().when(authService).confirmarEmail(12345678);

        mockMvc.perform(put("/auth/confirm-email/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmado exitosamente."));
    }

    @Test
    void confirmarEmail_illegalArgument_devuelve400() throws Exception {
        doThrow(new IllegalArgumentException("Código de verificación no encontrado.")).when(authService).confirmarEmail(99999999);

        mockMvc.perform(put("/auth/confirm-email/99999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código de verificación no encontrado."));
    }

    @Test
    void verificarEmailConfirmado_ok_devuelveBoolean() throws Exception {
        when(authService.usuarioVerificado(1L)).thenReturn(true);

        mockMvc.perform(get("/auth/email-confirmed/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    private static String crearSignupJson() {
        return "{" +
                "\"nombre\":\"Ana\"," +
                "\"primerApellido\":\"Pérez\"," +
                "\"segundoApellido\":\"García\"," +
                "\"username\":\"anaperez\"," +
                "\"email\":\"ana@cerebrus.com\"," +
                "\"password\":\"plain-pass\"," +
                "\"tipoUsuario\":\"ORGANIZACION\"," +
                "\"nombreCentro\":\"Centro Test\"" +
                "}";
    }

    private static String crearLoginJson(String identificador, String password) {
        return "{" +
                "\"identificador\":\"" + identificador + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";
    }

    private static Organizacion crearOrganizacion(Long id) {
        Organizacion org = new Organizacion();
        org.setId(id);
        org.setNombre("Org Test");
        org.setPrimerApellido("Apellido");
        org.setSegundoApellido("Segundo");
        org.setNombreUsuario("org" + id);
        org.setCorreoElectronico("org" + id + "@test.com");
        org.setContrasena("secret");
        org.setNombreCentro("Centro Test");
        org.setSuscripciones(new java.util.ArrayList<>());
        return org;
    }

    private static Maestro crearMaestro(Long id, Organizacion org) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        maestro.setNombre("Maestro Test");
        maestro.setPrimerApellido("Apellido");
        maestro.setSegundoApellido("Segundo");
        maestro.setNombreUsuario("maestro" + id);
        maestro.setCorreoElectronico("maestro" + id + "@test.com");
        maestro.setContrasena("secret");
        maestro.setOrganizacion(org);
        return maestro;
    }

    private static Alumno crearAlumno(Long id, Organizacion org) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        alumno.setNombre("Alumno Test");
        alumno.setPrimerApellido("Apellido");
        alumno.setSegundoApellido("Segundo");
        alumno.setNombreUsuario("alumno" + id);
        alumno.setCorreoElectronico("alumno" + id + "@test.com");
        alumno.setContrasena("secret");
        alumno.setPuntos(0);
        alumno.setOrganizacion(org);
        return alumno;
    }
}