package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.LoginRequest;
import com.tfg.ucm.dbcase.model.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final CookieService cookieService;
    private final UserService userService;

    public String verify(final LoginRequest loginRequest, HttpServletResponse response) {
        final Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(loginRequest.getUsername());
            cookieService.addHttpOnlyCookie("auth_token", token, 60 * 60 * 24, response);
            User user = userService.getCurrentUser(loginRequest.getUsername());
            return user.getUsername();
        }

        throw new BadCredentialsException("Credenciales incorrectos");
    }
}
