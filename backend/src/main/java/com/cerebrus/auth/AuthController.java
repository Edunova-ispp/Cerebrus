package com.cerebrus.auth;

import com.cerebrus.auth.payload.request.LoginRequest;
import com.cerebrus.auth.payload.request.SignupRequest;
import com.cerebrus.auth.payload.response.JwtResponse; 
import com.cerebrus.auth.payload.response.MessageResponse; 
import com.cerebrus.auth.security.JwtUtils;
import com.cerebrus.suscripcion.SuscripcionRepository;
import com.cerebrus.usuario.alumno.AlumnoRepository;
import com.cerebrus.usuario.maestro.MaestroRepository;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final MaestroRepository maestroRepository;
    private final AlumnoRepository alumnoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService, MaestroRepository maestroRepository, AlumnoRepository alumnoRepository, SuscripcionRepository suscripcionRepository, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.maestroRepository = maestroRepository;
        this.alumnoRepository = alumnoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        
        if (authService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El nombre de usuario ya está en uso."));
        }
        if (authService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El correo electrónico ya está en uso."));
        }

        try {
            authService.registrarUsuario(signUpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("¡Usuario registrado correctamente!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getIdentificador(), loginRequest.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if (roles.contains("MAESTRO") || roles.contains("ALUMNO")) {
                Long orgId = null;

                if (roles.contains("MAESTRO")) {
                    orgId = maestroRepository.findById(userDetails.getId())
                            .map(m -> m.getOrganizacion().getId()).orElse(null);
                } else {
                    orgId = alumnoRepository.findById(userDetails.getId())
                            .map(a -> a.getOrganizacion().getId()).orElse(null);
                }
                if (orgId == null || !suscripcionRepository.findByOrganizacionIdSuscripcionActiva(orgId).isPresent()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("CUENTA_ORG_NO_SUSCRIPCION"));
                }

            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles));
            
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("CUENTA_NO_VERIFICADA"));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Credenciales incorrectas"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // JWT es stateless, el backend solo confirma la acción. El frontend debe borrar el token.
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada. El cliente debe eliminar el token."));
    }
    @PutMapping("/confirm-email/{codigoVerificacion}")
    public ResponseEntity<?> confirmarEmail(@PathVariable Integer codigoVerificacion) {
        try {
            authService.confirmarEmail(codigoVerificacion);
            return ResponseEntity.ok(new MessageResponse("Email confirmado exitosamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/email-confirmed/{userId}")
    public ResponseEntity<Boolean> verificarEmailConfirmado(@PathVariable long userId) {
        
            boolean confirmado = authService.usuarioVerificado(userId);
            
                return ResponseEntity.ok(confirmado);
            
        }
}