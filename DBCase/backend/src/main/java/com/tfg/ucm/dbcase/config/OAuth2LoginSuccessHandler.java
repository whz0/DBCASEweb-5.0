package com.tfg.ucm.dbcase.config;

import com.tfg.ucm.dbcase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        final String username = oAuth2User.getAttribute("login");
        final String token = userService.processOAuthPostLogin(username);

        // TODO Cambiar por guardar en cookie
        System.out.println(token);

        final String targetUrl = "http://localhost:5173/";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
