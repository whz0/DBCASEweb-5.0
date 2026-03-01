package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.LoginRequest;
import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return service.verify(loginRequest);
    }

    @GetMapping("/me")
    public User getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return service.getCurrentUser(userDetails.getUsername());
    }

    @PostMapping("/logout")
    public void logout() {
        service.logout();
    }
}
