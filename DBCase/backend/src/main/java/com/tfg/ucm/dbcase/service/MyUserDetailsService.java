package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user =
                repo.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        final String password = Optional.of(user).map(User::getPassword).orElse("N/A");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(password)
                .build();
    }
}
