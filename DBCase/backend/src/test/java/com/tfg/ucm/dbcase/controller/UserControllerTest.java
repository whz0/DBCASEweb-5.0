package com.tfg.ucm.dbcase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ucm.dbcase.config.JWTFilter;
import com.tfg.ucm.dbcase.config.RateLimitingFilter;
import com.tfg.ucm.dbcase.config.SecurityConfig;
import com.tfg.ucm.dbcase.dto.LoginRequest;
import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.service.AuthService;
import com.tfg.ucm.dbcase.service.CookieService;
import com.tfg.ucm.dbcase.service.JWTService;
import com.tfg.ucm.dbcase.service.UserService;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = UserController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {SecurityConfig.class, JWTFilter.class, RateLimitingFilter.class})
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private JWTService jwtService;
    @MockitoBean private CookieService cookieService;
    @MockitoBean private PasswordEncoder passwordEncoder;

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        when(authService.verify(any(LoginRequest.class))).thenReturn("mock-token");

        // Act & Assert
        mockMvc.perform(
                        post("/api/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Unauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "wrong-pass");
        when(authService.verify(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    private static Stream<String> usernameProvider() {
        return Stream.of("user1", "admin", "test_user");
    }

    @ParameterizedTest(name = "Get current user for {0}")
    @MethodSource("usernameProvider")
    @WithMockUser
    void testGetMe(String username) throws Exception {
        User mockUser = new User();
        mockUser.setUsername(username);
        when(userService.getCurrentUser(any())).thenReturn(mockUser);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    @WithMockUser
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/user/logout")).andExpect(status().isOk());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        LoginRequest registerRequest = new LoginRequest("newuser", "password123");
        User savedUser = new User();
        savedUser.setUsername("newuser");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userService.createUser("newuser", "hashed-password")).thenReturn(savedUser);
        when(jwtService.generateToken("newuser")).thenReturn("mock-token");

        // Act & Assert
        mockMvc.perform(
                        post("/api/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void testRegister_UserExists() throws Exception {
        // Arrange
        LoginRequest registerRequest = new LoginRequest("existinguser", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userService.createUser("existinguser", "hashed-password"))
                .thenThrow(new IllegalArgumentException("El usuario ya existe"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
