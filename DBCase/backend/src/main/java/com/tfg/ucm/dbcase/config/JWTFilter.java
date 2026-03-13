package com.tfg.ucm.dbcase.config;

import com.tfg.ucm.dbcase.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            getJWTCookie(request)
                    .flatMap(jwtService::extractUsernameSafe)
                    .ifPresent(username -> authenticateUser(username, request));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        try {
            UserDetails userDetails = userService.loadUserByUsername(username);
            getJWTCookie(request)
                    .filter(token -> jwtService.validateToken(token, userDetails))
                    .ifPresent(
                            token -> {
                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                authToken.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                            });
        } catch (Exception ignored) {
        }
    }

    private Optional<String> getJWTCookie(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, "auth_token")).map(Cookie::getValue);
    }
}
