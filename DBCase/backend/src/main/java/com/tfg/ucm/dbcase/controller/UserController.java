package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public String login(@RequestBody User user) throws Exception {
        return service.verify(user);
    }

    @PostMapping("/logout")
    public void logout() {
        service.logout();
    }
}
