package com.cerebrus.auth.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthTokenFilter authTokenFilter;

    private static final String ORGANIZACION = "ORGANIZACION";
	private static final String MAESTRO = "MAESTRO";
    private static final String ALUMNO = "ALUMNO";


    public SecurityConfig(UserDetailsService userDetailsService, AuthTokenFilter authTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(this.userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000", 
                "http://localhost:5173",
                "https://cerebrus-frontend.onrender.com",
                "https://cerebrus-ylzn.onrender.com",
                "https://*.koyeb.app",
                "http://*.koyeb.app",
                "https://cerebrus-sprint3.onrender.com/",
                "https://cerebrus-edunova.onrender.com/",
                "https://cerebrus.koyeb.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/**",
                                "/error",
                                "/api/iaconnection/mock",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        // Conexión con la IA solo para usuarios autenticados
                        .requestMatchers("/api/iaconnection/**").authenticated()
                        // API restringida para que sea solo para alumnos´
                        .requestMatchers(HttpMethod.POST, "/api/actividades-alumno").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.DELETE, "/api/actividades-alumno/delete/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/actividades-alumno/ensure/{actividadId}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/actividades-alumno/{id}/abandon").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.PUT, "/api/actividades-alumno/corregir-automaticamente/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.PUT, "/api/actividades-alumno/corregir-automaticamente-general-clasificacion/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/respuestas-alumno-punto-imagen").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/respuestas-alumno-ordenacion").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/respuestas-alumno-general").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/respuestas-alumno-general/crucigrama/{crucigramaId}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/respuestas-alumno-general/abierta").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.PUT, "/api/respuestas-alumno-general/update/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.DELETE, "/api/respuestas-alumno-general/delete/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.POST, "/api/inscripciones/inscribe").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/ordenaciones/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/generales/cartas/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/generales/test/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/generales/clasificacion/{id}").hasAuthority(ALUMNO)
                        .requestMatchers(HttpMethod.GET, "/api/generales/abierta/{id}").hasAuthority(ALUMNO)
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}