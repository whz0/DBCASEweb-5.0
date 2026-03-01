package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.LoginRequest;
import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;

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

    public String processOAuthPostLogin(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            userRepository.save(newUser);
        }
        return jwtService.generateToken(username);
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username);
    }

    public void logout() {}
}
