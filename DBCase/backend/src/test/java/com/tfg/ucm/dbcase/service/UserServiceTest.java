package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.model.User;
import com.tfg.ucm.dbcase.repository.UserRepository;
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
                Arguments.of("newUser", null, 1), Arguments.of("existingUser", new User(), 0));
    }

    @ParameterizedTest(name = "Processing OAuth login for {0}")
    @MethodSource("oauthLoginProvider")
    void testProcessOAuthPostLogin(String username, User repoReturn, int expectedSaveCalls) {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(repoReturn);
        when(jwtService.generateToken(username)).thenReturn("mock-jwt-token");

        // Act
        String token = userService.processOAuthPostLogin(username);

        // Assert
        assertEquals("mock-jwt-token", token);
        verify(userRepository, times(expectedSaveCalls)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(username);
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
        when(userRepository.findByUsername(username)).thenReturn(mockUser);

        // Act
        User result = userService.getCurrentUser(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }
}
