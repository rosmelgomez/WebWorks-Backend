package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;


import com.upc.webworksbackend.dto.CompanyDto;
import com.upc.webworksbackend.dto.UserDto;
import com.upc.webworksbackend.dto.UserLoginDto;
import com.upc.webworksbackend.security.JwtTokenUtil;
import com.upc.webworksbackend.serviceimplements.JwtUserDetailsService;
import com.upc.webworksbackend.serviceinterface.CompanyService;
import com.upc.webworksbackend.serviceinterface.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping()
public class LoginRegisterController {

    @Value("${app.security.cookie.access.name}")
    private String accessCookieName;

    @Value("${app.security.cookie.api.name}")
    private String apiCookieName;

    @Value("${app.security.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.security.cookie.same-site}")
    private String sameSite;

    @Value("${app.security.cookie.api.path}")
    private String apiCookiePath;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final UserService userService;
    private final CompanyService companyService;

    public LoginRegisterController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                                   JwtUserDetailsService jwtUserDetailsService, UserService userService,
                                   CompanyService companyService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.userService = userService;
        this.companyService = companyService;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<UserDto> agregarUser(@RequestBody UserDto userDto) {
        userDto.setRol("DEVELOPER");
        return new ResponseEntity<>(userService.addUser(userDto), HttpStatus.CREATED);
    }

    @PostMapping("/registerCompany")
    public ResponseEntity<CompanyDto> agregarCompany(@RequestBody CompanyDto companyDto) {
        companyDto.setRol("COMPANY");
        return new ResponseEntity<>(companyService.addCompany(companyDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticateUser(@RequestBody UserLoginDto user) throws Exception {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Usuario y contrasena requeridos");
        }
        authenticateUser(user.getUsername(), user.getPassword());
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.getUsername());
        final String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String apiToken = jwtTokenUtil.generateApiToken(userDetails);

        ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, accessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite(sameSite)
                .maxAge(jwtTokenUtil.getAccessTokenValiditySeconds())
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(apiCookieName, apiToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path(apiCookiePath)
                .sameSite(sameSite)
                .maxAge(jwtTokenUtil.getRefreshTokenValiditySeconds())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login Successfull");
    }

    // datos de la sesion actual (el frontend no puede leer el jwt de la cookie httpOnly)
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    public ResponseEntity<Map<String, String>> sessionUser(Authentication authentication) {
        String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        return ResponseEntity.ok(Map.of("username", authentication.getName(), "rol", rol));
    }

    @GetMapping("/api-token")
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    public ResponseEntity<?> validateApiToken(HttpServletRequest request) {
        String apiToken = getCookieValue(request, apiCookieName);
        if (apiToken == null || apiToken.isBlank()) {
            return ResponseEntity.status(401).body("Api token requerido");
        }

        final String username;
        try {
            username = jwtTokenUtil.getUsernameFromToken(apiToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Api token invalido");
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
        if (!jwtTokenUtil.validateApiToken(apiToken, userDetails)) {
            return ResponseEntity.status(401).body("Api token invalido");
        }

        return ResponseEntity.ok("Api token valido");
    }

    @PostMapping({ "/logout", "/logout-user" })
    public ResponseEntity<?> logoutUser() {
        ResponseCookie deleteAccessCookie = ResponseCookie.from(accessCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite(sameSite)
                .maxAge(0)
                .build();

        ResponseCookie deleteApiCookie = ResponseCookie.from(apiCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path(apiCookiePath)
                .sameSite(sameSite)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteApiCookie.toString())
                .body("Logout Successfull");
    }

    private @Nullable String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void authenticateUser(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
