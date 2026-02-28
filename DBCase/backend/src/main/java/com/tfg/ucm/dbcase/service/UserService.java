package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authManager;


    public String verify(User user) throws Exception {
        try {
            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            if (authentication.isAuthenticated())
                return jwtService.generateToken(user.getUsername());
        }
        catch (Exception e) {
            throw new Exception("Algo falló");
        }
        throw new Exception("Credenciales incorrectos");
    }


    public void logout() {
    }
}
