package com.t1.officebooking.authorization.security;

import com.t1.officebooking.model.UserRole;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }


    public Set<UserRole> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }
}