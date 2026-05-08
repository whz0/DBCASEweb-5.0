package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.model.UserSettings;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public String processOAuthPostLogin(String username, String picture) {
        User user =
                userRepository
                        .findByUsername(username)
                        .map(
                                existingUser -> {
                                    refreshExpiry(existingUser);
                                    return updatePictureIfNeeded(existingUser, picture);
                                })
                        .orElseGet(() -> createOauth2User(username, picture));

        return jwtService.generateToken(user.getUsername());
    }

    private User createOauth2User(String username, String picture) {
        final User user =
                User.builder()
                        .username(username)
                        .pictureUrl(picture)
                        .expiresAt(LocalDateTime.now().plusYears(1))
                        .build();
        return userRepository.save(user);
    }

    private User updatePictureIfNeeded(User user, String picture) {
        if (picture != null && !picture.equals(user.getPictureUrl())) {
            user.setPictureUrl(picture);
            return userRepository.save(user);
        }
        return user;
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
        final User user =
                User.builder()
                        .username(username)
                        .password(password)
                        .expiresAt(LocalDateTime.now().plusYears(1L))
                        .build();
        return userRepository.save(user);
    }

    public User updateChart(String username, String chart) {
        User user = getCurrentUser(username);
        user.setChart(chart);
        return userRepository.save(user);
    }

    public User updateSettings(String username, UserSettings settings) {
        User user = getCurrentUser(username);
        user.setSettings(settings);
        return userRepository.save(user);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredUsers() {
        userRepository.deleteByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime.now());
    }

    public void refreshExpiryByUsername(String username) {
        userRepository.findByUsername(username).ifPresent(this::refreshExpiry);
    }

    private void refreshExpiry(User user) {
        if (user.getExpiresAt() != null) {
            user.setExpiresAt(LocalDateTime.now().plusYears(1));
            userRepository.save(user);
        }
    }
}
