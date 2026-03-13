package com.tfg.ucm.dbcase.config;

import com.tfg.ucm.dbcase.service.CookieService;
import com.tfg.ucm.dbcase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String GOOGLE = "google";
    private static final String GITHUB = "github";

    private static final Map<String, String> PROVIDERS =
            Map.of(
                    GOOGLE, "email",
                    GITHUB, "login");

    private final UserService userService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        final OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        final String provider = oauthToken.getAuthorizedClientRegistrationId();
        final OAuth2User oAuth2User = oauthToken.getPrincipal();

        String username =
                Optional.ofNullable(provider)
                        .map(String::toLowerCase)
                        .map(PROVIDERS::get)
                        .map(oAuth2User::getAttribute)
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .orElseGet(oAuth2User::getName);

        final String token = userService.processOAuthPostLogin(username);

        cookieService.addHttpOnlyCookie("auth_token", token, 60 * 60 * 15, response);

        final String targetUrl =
                UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/success")
                        .queryParam("provider", provider)
                        .build()
                        .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
