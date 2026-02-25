package com.cerebrus.auth.payload.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id; 
    private String username;
    private List<String> roles;


    public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "JwtResponse [token=" + token + ", type=" + type + ", id=" + id + ", username=" + username + ", roles=" + roles + "]";
    }
}