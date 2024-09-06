package com.tim.projectmanagement.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.model.UserPrincipal;
import com.tim.projectmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.tim.projectmanagement.constant.Constants.AUTHORITIES;
import static com.tim.projectmanagement.constant.Constants.TOKEN_PREFIX;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final UserService userService;

    @Value("${jwt.secret}")
    private String secret;

    String[] getUserAuthoritiesAsArray(UserPrincipal user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }

    List<GrantedAuthority> getAuthoritiesFromTokenAsList(String token) {
        return Arrays.stream(getAuthoritiesArrayFromToken(token))
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
    }

    String[] getAuthoritiesArrayFromToken(String token) {
        return getTokenVerifier()
                .verify(token)
                .getClaim(AUTHORITIES)
                .asArray(String.class);
    }

    JWTVerifier getTokenVerifier() {
        try {
            return JWT.require(getAlgorithm())
                    .build();
        } catch (JWTVerificationException e) {
            log.error("Failed to create JWT verifier: {}", e.getMessage());
            throw new JWTVerificationException("Failed to create JWT verifier");
        }
    }

    public boolean isValidToken(Long id, String token) {
        return !Objects.isNull(id) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getTokenVerifier()
                .verify(token)
                .getExpiresAt()
                .before(new Date());
    }

    Algorithm getAlgorithm() {
        try {
            validateSecret();

            return Algorithm.HMAC512(secret);
        } catch (IllegalArgumentException e) {
            log.error("Invalid secret provided for HMAC512 algorithm", e);
            throw new IllegalArgumentException("Invalid secret provided", e);
        }
    }

    public Long getSubjectFromToken(String token, HttpServletRequest request) {
        try {
            return  Long.valueOf(getTokenVerifier()
                    .verify(token)
                    .getSubject());
        } catch (TokenExpiredException e) {
            request.setAttribute("expiredMessage", e.getMessage());
            throw new ApiException("Invalid claim in token");
        } catch (InvalidClaimException e) {
            request.setAttribute("invalidClaim", e.getMessage());
            throw new ApiException("Invalid claim in token");
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }

    private void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            log.error("JWT secret is not configured properly");
            throw new IllegalStateException("JWT secret is not configured");
        }
    }

    Authentication getAuthentication(Long id, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UserDTO user = userService.findUserById(id);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
        );
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return token;
    }

    public boolean isRefreshToken(String token) {
        try {
            JWTVerifier verifier = getTokenVerifier();
            DecodedJWT jwt = verifier.verify(token);
            return "refresh".equals(jwt.getClaim("token_type").asString());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(7);
        }

        return null;
    }
}
