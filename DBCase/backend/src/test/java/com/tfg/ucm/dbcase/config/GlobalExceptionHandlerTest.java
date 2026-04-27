package com.tfg.ucm.dbcase.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static Stream<Arguments> authExceptionProvider() {
        return Stream.of(
                Arguments.of(new BadCredentialsException("Bad credentials")),
                Arguments.of(new BadCredentialsException("Invalid token")),
                Arguments.of(new BadCredentialsException("")));
    }

    @ParameterizedTest(name = "AuthenticationException: {0}")
    @MethodSource("authExceptionProvider")
    void testHandleAuthenticationException(AuthenticationException ex) {
        ResponseEntity<?> response = handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ex.getMessage(), ((java.util.Map<?, ?>) response.getBody()).get("message"));
    }

    private static Stream<Arguments> generalExceptionProvider() {
        return Stream.of(
                Arguments.of(new Exception("Something went wrong")),
                Arguments.of(new RuntimeException("NPE")),
                Arguments.of(new IllegalArgumentException("Bad input")));
    }

    @ParameterizedTest(name = "GeneralException: {0}")
    @MethodSource("generalExceptionProvider")
    void testHandleGeneralException(Exception ex) {
        ResponseEntity<?> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String message = (String) ((java.util.Map<?, ?>) response.getBody()).get("message");
        assertEquals("Internal Server Error: " + ex.getMessage(), message);
    }
}
