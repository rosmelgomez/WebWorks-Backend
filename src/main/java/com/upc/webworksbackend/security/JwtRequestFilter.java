package com.upc.webworksbackend.security;

import com.upc.webworksbackend.serviceimplements.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsServ;

    @Value(value = "${app.security.cookie.access.name}")
    private String accessCookieName;

    @Value(value = "${app.security.cookie.api.name}")
    private String apiCookieName;

    @Value(value = "${app.security.cookie.secure}")
    private boolean secureCookie;

    @Value(value = "${app.security.cookie.same-site}")
    private String sameSite;

    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, JwtUserDetailsService jwtUserDetailsServ) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtUserDetailsServ = jwtUserDetailsServ;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final boolean useApiToken = requiresApiToken(request);
        String username = null;
        String jwtToken = resolveToken(request, useApiToken);

        if (jwtToken != null) {
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.debug("No se puede encontrar el token en la petición");
            } catch (ExpiredJwtException e) {
                log.debug("El token JWT ha expirado");
            } catch (Exception e) {
                log.debug("Token JWT inválido");
            }
        }

        // Si el usuario no fue autenticado (el access token no existe o expiro),
        // y no estamos en una ruta que requiere explicitamente el api token,
        // intentamos refrescarlo en el backend de forma transparente.
        if (username == null && !useApiToken) {
            String apiToken = resolveToken(request, true); // true = resolver apiCookieName
            if (apiToken != null) {
                try {
                    String refreshUsername = jwtTokenUtil.getUsernameFromToken(apiToken);
                    if (refreshUsername != null) {
                        UserDetails userDetails = jwtUserDetailsServ.loadUserByUsername(refreshUsername);
                        if (jwtTokenUtil.validateApiToken(apiToken, userDetails)) {
                            // Generamos un nuevo access_token
                            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);

                            // Inyectamos la nueva cookie en la respuesta HTTP
                            ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, newAccessToken)
                                    .httpOnly(true)
                                    .secure(secureCookie)
                                    .path("/")
                                    .sameSite(sameSite)
                                    .maxAge(jwtTokenUtil.getAccessTokenValiditySeconds())
                                    .build();

                            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

                            // Autenticamos al usuario para la peticion actual
                            username = refreshUsername;
                            jwtToken = newAccessToken;
                            log.debug("Access token regenerado y refrescado de forma transparente en el Backend.");
                        }
                    }
                } catch (Exception ex) {
                    log.debug("No se pudo refrescar el token de acceso desde el api token: {}", ex.getMessage());
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailsServ.loadUserByUsername(username);
            //// verificando
            boolean validToken = useApiToken
                    ? jwtTokenUtil.validateApiToken(jwtToken, userDetails)
                    : jwtTokenUtil.validateAccessToken(jwtToken, userDetails);

            if (validToken) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private @Nullable String resolveToken(HttpServletRequest request, boolean useApiToken) {
        String tokenFromCookie = getTokenFromCookie(request, useApiToken ? apiCookieName : accessCookieName);
        if (tokenFromCookie != null && !tokenFromCookie.isBlank()) {
            return tokenFromCookie;
        }
        return null;
    }

    private @Nullable String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }

        }
        return null;

    }

    private boolean requiresApiToken(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return requestPath.endsWith("/api-token")
                || requestPath.endsWith("/refresh-token")
                || requestPath.endsWith("/logout")
                || requestPath.endsWith("/logout-user");
    }
}
