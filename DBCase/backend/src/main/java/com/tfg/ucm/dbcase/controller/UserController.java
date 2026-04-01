package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.LoginRequest;
import com.tfg.ucm.dbcase.dto.RegisterRequest;
import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.service.AuthService;
import com.tfg.ucm.dbcase.service.CookieService;
import com.tfg.ucm.dbcase.service.JWTService;
import com.tfg.ucm.dbcase.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JWTService jwtService;
    private final CookieService cookieService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<User> login(
            @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        final String token = authService.verify(loginRequest);
        cookieService.addHttpOnlyCookie("auth_token", token, 60 * 60 * 24, response);

        final String username = jwtService.extractUsername(token);
        final User user = userService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        cookieService.deleteCookie("auth_token", response);
        return ResponseEntity.ok("Sesión cerrada");
    }

    @PostMapping("/chart")
    public ResponseEntity<User> saveChart(
            @RequestBody String chart, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.updateChart(userDetails.getUsername(), chart);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        try {
            User user =
                    userService.createUser(
                            registerRequest.getUsername(),
                            passwordEncoder.encode(registerRequest.getPassword()));

            String token = jwtService.generateToken(user.getUsername());
            cookieService.addHttpOnlyCookie("auth_token", token, 60 * 60 * 24, response);

            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
