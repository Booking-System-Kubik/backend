package com.t1.officebooking.authorization.service;

import com.t1.officebooking.authorization.dto.JwtResponse;
import com.t1.officebooking.authorization.dto.LoginRequest;
import com.t1.officebooking.authorization.dto.LoginResponse;
import com.t1.officebooking.authorization.dto.RegistrationRequest;
import com.t1.officebooking.authorization.model.RevokedToken;
import com.t1.officebooking.authorization.repository.RevokedTokenRepository;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.LocationService;
import com.t1.officebooking.service.OrganizationService;
import com.t1.officebooking.service.PendingRegistrationService;
import com.t1.officebooking.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private LocationService locationService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private RevokedTokenRepository revokedTokenRepository;
    @Mock
    private PendingRegistrationService pendingRegistrationService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailExists_throws() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("a@b.com");
        req.setPassword("secret12");
        req.setFullName("Full Name Here");
        req.setPosition("Dev");

        when(userService.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_existingOrganization_createsPendingAndReturnsTrue() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("user@corp.com");
        req.setPassword("secret12");
        req.setFullName("Full Name Here");
        req.setPosition("Dev");
        req.setOrganizationId(5L);

        when(userService.existsByEmail("user@corp.com")).thenReturn(false);
        Organization org = new Organization("Corp");
        org.setId(5L);
        when(organizationService.findById(5L)).thenReturn(org);
        when(passwordEncoder.encode("secret12")).thenReturn("hash");

        boolean pending = authService.register(req);

        assertThat(pending).isTrue();
        verify(pendingRegistrationService).create(
                eq("Full Name Here"),
                eq("user@corp.com"),
                eq("Dev"),
                eq("hash"),
                eq(5L),
                isNull());
        verify(userService, never()).saveUser(any());
    }

    @Test
    void register_newOrganization_createsUserWithAdminWorkspaceRole() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("new@corp.com");
        req.setPassword("secret12");
        req.setFullName("Full Name Here");
        req.setPosition("Dev");
        req.setOrganizationName("  New Org  ");

        when(userService.existsByEmail("new@corp.com")).thenReturn(false);
        Organization created = new Organization("New Org");
        created.setId(9L);
        when(organizationService.create("New Org")).thenReturn(created);
        when(passwordEncoder.encode("secret12")).thenReturn("hash");

        boolean pending = authService.register(req);

        assertThat(pending).isFalse();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getRoles()).contains(UserRole.ROLE_USER, UserRole.ROLE_ADMIN_WORKSPACE);
        assertThat(saved.getOrganization()).isSameAs(created);
    }

    @Test
    void authenticate_wrongPassword_throws() {
        LoginRequest login = new LoginRequest();
        login.setEmail("a@b.com");
        login.setPassword("wrong");

        User user = new User();
        user.setHashedPassword("encoded");
        when(userService.findByEmail("a@b.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(login))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void authenticate_success_returnsLoginResponse() {
        LoginRequest login = new LoginRequest();
        login.setEmail("a@b.com");
        login.setPassword("ok");

        User user = new User();
        user.setHashedPassword("encoded");
        user.setRoles(Set.of(UserRole.ROLE_USER));
        JwtResponse tokens = new JwtResponse("a", "r", 60, 86400);
        when(userService.findByEmail("a@b.com")).thenReturn(user);
        when(passwordEncoder.matches("ok", "encoded")).thenReturn(true);
        when(jwtTokenService.generateTokens(user)).thenReturn(tokens);

        LoginResponse response = authService.authenticate(login);

        assertThat(response.jwtResponse()).isSameAs(tokens);
        assertThat(response.role()).containsExactly(UserRole.ROLE_USER);
    }

    @Test
    void refreshToken_whenRevoked_throwsExpiredJwtException() {
        when(jwtTokenService.validateToken("rt")).thenReturn(true);
        when(jwtTokenService.isTokenRevoked("rt")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("rt"))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void revokeToken_savesRevokedByJti() {
        when(jwtTokenService.extractJti("tok")).thenReturn("jti-1");

        authService.revokeToken("tok");

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getJti()).isEqualTo("jti-1");
    }

    @Test
    void checkAuth_delegatesToUserService() {
        UUID id = UUID.randomUUID();
        when(userService.getUserData(id)).thenReturn(mock(com.t1.officebooking.authorization.dto.UserDataResponse.class));

        authService.checkAuth(id);

        verify(userService).getUserData(id);
    }
}
