package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock private HttpServletResponse response;

    @InjectMocks private CookieService cookieService;

    @Captor private ArgumentCaptor<Cookie> cookieCaptor;

    private static Stream<Arguments> cookieProvider() {
        return Stream.of(
                Arguments.of("auth_token", "jwt-token-123", 3600),
                Arguments.of("session_id", "session-abc", 7200),
                Arguments.of("refresh_token", "refresh-xyz", 86400));
    }

    @ParameterizedTest(name = "Add cookie: {0} with maxAge {2}")
    @MethodSource("cookieProvider")
    void testAddHttpOnlyCookie(String name, String value, int maxAge) {
        // Act
        cookieService.addHttpOnlyCookie(name, value, maxAge, response);

        // Assert
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertNotNull(cookie);
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertEquals(maxAge, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }

    private static Stream<String> cookieNameProvider() {
        return Stream.of("auth_token", "session_id", "refresh_token", "user_pref");
    }

    @ParameterizedTest(name = "Delete cookie: {0}")
    @MethodSource("cookieNameProvider")
    void testDeleteCookie(String name) {
        // Act
        cookieService.deleteCookie(name, response);

        // Assert
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertNotNull(cookie);
        assertEquals(name, cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }
}
