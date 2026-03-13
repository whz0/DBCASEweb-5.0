package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.LoginRequest;
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

    public String verify(final LoginRequest loginRequest) {
        final Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(loginRequest.getUsername());
        }

        throw new BadCredentialsException("Credenciales incorrectos");
    }
}
