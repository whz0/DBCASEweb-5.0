package com.tfg.ucm.dbcase.config;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.service.CookieService;
import com.tfg.ucm.dbcase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2LoginSuccessHandlerTest {

    private static Stream<Arguments> providerProvider() {
        return Stream.of(
                Arguments.of("google", "user@example.com", "https://pic.google.com/photo"),
                Arguments.of("github", "githubuser", "https://avatars.github.com/1"));
    }

    @ParameterizedTest(name = "OAuth2 login with provider={0}")
    @MethodSource("providerProvider")
    void testOnAuthenticationSuccess(String provider, String username, String picture)
            throws Exception {
        UserService userService = mock(UserService.class);
        CookieService cookieService = mock(CookieService.class);

        OAuth2LoginSuccessHandler handler =
                new OAuth2LoginSuccessHandler(userService, cookieService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://localhost:5173");

        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        handler.setRedirectStrategy(redirectStrategy);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        String usernameAttr = provider.equals("google") ? "email" : "login";
        String pictureAttr = provider.equals("google") ? "picture" : "avatar_url";
        when(oAuth2User.getAttribute(usernameAttr)).thenReturn(username);
        when(oAuth2User.getAttribute(pictureAttr)).thenReturn(picture);

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(token.getPrincipal()).thenReturn(oAuth2User);

        when(userService.processOAuthPostLogin(anyString(), anyString())).thenReturn("jwt-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.onAuthenticationSuccess(request, response, token);

        verify(userService).processOAuthPostLogin(eq(username), eq(picture));
        verify(cookieService)
                .addHttpOnlyCookie(eq("auth_token"), eq("jwt-token"), anyInt(), eq(response));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), anyString());
    }
}
