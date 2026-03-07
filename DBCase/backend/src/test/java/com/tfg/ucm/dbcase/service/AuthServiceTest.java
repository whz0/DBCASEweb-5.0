package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tfg.ucm.dbcase.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JWTService jwtService;

    @Mock private AuthenticationManager authManager;

    @InjectMocks private AuthService authService;

    @InjectMocks private CookieService cookieService;

    @Test
    void testVerify_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        Authentication authentication = mock(Authentication.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("user")).thenReturn("mock-token");

        // Act
        authService.verify(loginRequest, response);

        // Assert
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertEquals("token", cookie.getName());
        assertEquals("mock-token", cookie.getValue());
    }

    @Test
    void testVerify_NotAuthenticated() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        Authentication authentication = mock(Authentication.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act & Assert
        assertThrows(
                BadCredentialsException.class, () -> authService.verify(loginRequest, response));
    }

    @Test
    void testVerify_AuthenticationThrowsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(
                BadCredentialsException.class, () -> authService.verify(loginRequest, response));
    }
}
