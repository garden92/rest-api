package com.kt.kol.app.security;

import java.util.List;

public record JwtUserPrincipal(
    String userId,
    String username,
    List<String> roles
) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}