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

    public String processOAuthPostLogin(String username, String picture) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;
        if (userOptional.isEmpty()) {
            user = new User();
            user.setUsername(username);
            user.setPictureUrl(picture);
            userRepository.save(user);
        } else {
            user = userOptional.get();
            if (picture != null && !picture.equals(user.getPictureUrl())) {
                user.setPictureUrl(picture);
                userRepository.save(user);
            }
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

    public User updateChart(String username, String chart) {
        User user = getCurrentUser(username);
        user.setChart(chart);
        return userRepository.save(user);
    }
}
