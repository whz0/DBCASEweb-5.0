package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.model.UserSettings;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private JWTService jwtService;

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private static Stream<Arguments> oauthLoginProvider() {
        return Stream.of(
                Arguments.of("newUser", null, "pic1", 1),
                Arguments.of(
                        "existingUser", User.builder().username("existingUser").build(), "pic2", 1),
                Arguments.of(
                        "existingUserWithSamePic",
                        User.builder()
                                .id(1L)
                                .username("existingUserWithSamePic")
                                .password("pass")
                                .chart("chart")
                                .pictureUrl("samePic")
                                .build(),
                        "samePic",
                        0));
    }

    @ParameterizedTest(name = "Processing OAuth login for {0}")
    @MethodSource("oauthLoginProvider")
    void testProcessOAuthPostLogin(
            String username, User repoReturn, String picture, int expectedSaveCalls) {

        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(repoReturn));

        when(jwtService.generateToken(any())).thenReturn("mock-jwt-token");

        lenient()
                .when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = userService.processOAuthPostLogin(username, picture);

        // Assert
        assertEquals("mock-jwt-token", token);
        verify(userRepository, times(expectedSaveCalls)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any());
    }

    private static Stream<String> usernameProvider() {
        return Stream.of("alice", "bob", "charlie");
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testGetCurrentUser(String username) {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getCurrentUser(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testCreateUser_Success(String username) {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        User savedUser = new User();
        savedUser.setUsername(username);
        savedUser.setPassword("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(username, "hashed-password");

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testCreateUser_UserExists(String username) {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // Act & Assert
        try {
            userService.createUser(username, "password");
        } catch (IllegalArgumentException e) {
            assertEquals("El usuario ya existe", e.getMessage());
        }
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testGetCurrentUser_NotFound(String username) {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentUser(username));
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testUpdateChart(String username) {
        User user = User.builder().username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateChart(username, "chart-data");

        assertEquals("chart-data", result.getChart());
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @MethodSource("usernameProvider")
    void testUpdateSettings(String username) {
        User user = User.builder().username(username).build();
        UserSettings settings = new UserSettings("en", "dark");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateSettings(username, settings);

        assertEquals(settings, result.getSettings());
        verify(userRepository).save(user);
    }

    @Test
    void testRefreshExpiryByUsername_WithExpiry() {
        User user = User.builder().username("u").expiresAt(LocalDateTime.now().plusDays(1)).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.refreshExpiryByUsername("u");

        verify(userRepository).save(user);
    }

    @Test
    void testRefreshExpiryByUsername_NullExpiry_DoesNotSave() {
        User user = User.builder().username("u").expiresAt(null).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));

        userService.refreshExpiryByUsername("u");

        verify(userRepository, never()).save(any());
    }

    @Test
    void testRefreshExpiryByUsername_UserNotFound_DoesNothing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        userService.refreshExpiryByUsername("ghost");
        verify(userRepository, never()).save(any());
    }
}
