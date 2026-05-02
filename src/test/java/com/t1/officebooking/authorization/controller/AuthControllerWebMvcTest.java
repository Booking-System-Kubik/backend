package com.t1.officebooking.authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t1.officebooking.authorization.dto.JwtResponse;
import com.t1.officebooking.authorization.dto.LoginRequest;
import com.t1.officebooking.authorization.dto.LoginResponse;
import com.t1.officebooking.authorization.dto.RefreshTokenRequest;
import com.t1.officebooking.authorization.dto.RegistrationRequest;
import com.t1.officebooking.authorization.service.AuthService;
import com.t1.officebooking.authorization.service.JwtTokenService;
import com.t1.officebooking.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void register_whenPending_returnsAccepted() throws Exception {
        RegistrationRequest body = new RegistrationRequest(
                "valid@mail.com",
                "secret12",
                "Full Name Here",
                "Engineer",
                null,
                1L,
                null
        );
        when(authService.register(any(RegistrationRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted());

        verify(authService).register(any(RegistrationRequest.class));
    }

    @Test
    void register_whenUserCreated_returnsOk() throws Exception {
        RegistrationRequest body = new RegistrationRequest(
                "valid@mail.com",
                "secret12",
                "Full Name Here",
                "Engineer",
                null,
                null,
                "New Org"
        );
        when(authService.register(any(RegistrationRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void login_returnsOk() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("a@b.com");
        login.setPassword("secret12");

        JwtResponse tokens = new JwtResponse("acc", "ref", 60, 86400);
        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(tokens, Set.of(UserRole.ROLE_USER)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_returnsOk() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rt");

        when(authService.refreshToken("rt")).thenReturn(new JwtResponse("a", "r", 1, 2));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void logout_returnsOk() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rt");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).revokeToken("rt");
    }
}
