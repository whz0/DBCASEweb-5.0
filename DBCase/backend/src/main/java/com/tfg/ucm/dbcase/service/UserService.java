package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public String processOAuthPostLogin(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            userRepository.save(newUser);
        }
        return jwtService.generateToken(username);
    }

    public User getCurrentUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public User createUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }
}
