package com.cerebrus.auth;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.cerebrus.usuario.Alumno;
import com.cerebrus.usuario.Director;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

// ADAPTADOR ENTRE NUESTRO USUARIO Y EL USERDETAILS DE SPRING SECURITY
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario user) {
        
        String nombreRol = "USUARIO"; 

        if (user instanceof Alumno) {
            nombreRol = "ALUMNO";
        } else if (user instanceof Maestro) {
            nombreRol = "MAESTRO";
        } else if (user instanceof Director) {
            nombreRol = "DIRECTOR";
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(nombreRol));

        return new UserDetailsImpl(
                user.getId(), 
                user.getNombreUsuario(),
                user.getContrasena(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserDetailsImpl other = (UserDetailsImpl) obj;
        return Objects.equals(id, other.id);
    }
}