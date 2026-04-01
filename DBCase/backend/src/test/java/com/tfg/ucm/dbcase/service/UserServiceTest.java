package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private JWTService jwtService;

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private static Stream<Arguments> oauthLoginProvider() {
        return Stream.of(
                Arguments.of("newUser", null, "pic1", 1),
                Arguments.of(
                        "existingUser",
                        new User(null, "existingUser", null, null, null),
                        "pic2",
                        1),
                Arguments.of(
                        "existingUserWithSamePic",
                        new User(1L, "existingUserWithSamePic", "pass", "chart", "samePic"),
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
}
