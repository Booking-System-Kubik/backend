package com.t1.officebooking.authorization.security;

import com.t1.officebooking.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void getRoles_mapsAuthoritiesToEnumValues() {
        CustomUserDetails details = new CustomUserDetails(
                "id-1",
                "user@test.com",
                List.of(
                        new SimpleGrantedAuthority(UserRole.ROLE_USER.name()),
                        new SimpleGrantedAuthority(UserRole.ROLE_ADMIN_PROJECT.name())
                )
        );

        assertThat(details.getRoles()).containsExactlyInAnyOrder(
                UserRole.ROLE_USER,
                UserRole.ROLE_ADMIN_PROJECT
        );
        assertThat(details.getUsername()).isEqualTo("user@test.com");
        assertThat(details.getUserId()).isEqualTo("id-1");
    }
}
