package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final CookieService cookieService;

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

    public void logout(HttpServletResponse response) {
        cookieService.deleteCookie("auth_token", response);
    }
}
