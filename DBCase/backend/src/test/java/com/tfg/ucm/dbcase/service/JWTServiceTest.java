package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JWTServiceTest {

    @Autowired private JWTService jwtService;

    //    @Test
    //    void testExceptionIsCaughtInConstructor() {
    //        try (MockedStatic<KeyGenerator> generatorMockedStatic =
    //                Mockito.mockStatic(KeyGenerator.class)) {
    //            generatorMockedStatic
    //                    .when(() -> KeyGenerator.getInstance(any(String.class)))
    //                    .thenThrow(NoSuchAlgorithmException.class);
    //            assertThrows(
    //                    RuntimeException.class,
    //                    () -> {
    //                        final JWTService ignored = new JWTService();
    //                    });
    //        }
    //    }

    private static Stream<String> usernamesProvider() {
        return Stream.of("user1", "admin", "test_user", "user-with-hyphen", "user.with.dots");
    }

    @ParameterizedTest
    @MethodSource("usernamesProvider")
    void testGenerateAndExtractUsername(String username) {
        final String token = jwtService.generateToken(username);
        assertNotNull(token);

        final String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    private static Stream<Arguments> tokenValidationProvider() {
        return Stream.of(
                Arguments.of("user1", "user1", true),
                Arguments.of("admin", "admin", true),
                Arguments.of("user1", "user2", false),
                Arguments.of("admin", "root", false),
                Arguments.of("john", "doe", false));
    }

    @ParameterizedTest(name = "Token for {0} validated against {1}, expected: {2}")
    @MethodSource("tokenValidationProvider")
    void testValidateToken(String tokenUsername, String detailsUsername, boolean expected) {
        final String token = jwtService.generateToken(tokenUsername);
        final UserDetails userDetails =
                new User(detailsUsername, "password", Collections.emptyList());

        assertEquals(expected, jwtService.validateToken(token, userDetails));
    }
}
