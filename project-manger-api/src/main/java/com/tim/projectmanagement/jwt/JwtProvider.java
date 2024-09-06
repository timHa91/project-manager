package com.tim.projectmanagement.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.tim.projectmanagement.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.tim.projectmanagement.constant.Constants.AUTHORITIES;
import static java.lang.System.currentTimeMillis;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtUtils jwtUtils;

    @Value("${jwt.access-token-expiration-time}")
    private Long accessTokenExpirationTime;

    @Value("${jwt.refresh-token-expiration-time}")
    private Long refreshTokenExpirationTime;

    public String generateAccessToken(UserPrincipal user) {
        try {
            return JWT.create()
                    .withIssuedAt(new Date())
                    .withSubject(String.valueOf(user.getUser().getUserId()))
                    .withExpiresAt(new Date(currentTimeMillis() + accessTokenExpirationTime))
                    .withArrayClaim(AUTHORITIES, jwtUtils.getUserAuthoritiesAsArray(user))
                    .sign(jwtUtils.getAlgorithm());
        } catch (JWTCreationException e) {
            throw new JWTVerificationException("Error creating the JWT");
        }
    }

    public String generateRefreshToken(UserPrincipal user) {
        try {
            return JWT.create()
                    .withIssuedAt(new Date())
                    .withSubject(String.valueOf(user.getUser().getUserId()))
                    .withExpiresAt(new Date(currentTimeMillis() + refreshTokenExpirationTime))
                    .withClaim("token_type", "refresh")
                    .withArrayClaim(AUTHORITIES, jwtUtils.getUserAuthoritiesAsArray(user))
                    .sign(jwtUtils.getAlgorithm());
        } catch (JWTCreationException e) {
            throw new JWTVerificationException("Error creating the JWT");
        }
    }
}
