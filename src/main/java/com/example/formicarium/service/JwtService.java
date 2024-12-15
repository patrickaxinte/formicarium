package com.example.formicarium.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // generare token
    public String generateToken(UserDetails userDetails) {
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(Algorithm.HMAC512(secretKey));
    }

    // validare token
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(userDetails.getUsername())
                    .build();
            verifier.verify(token); // daca verificarea trece, token-ul este valid
            return true;
        } catch (JWTVerificationException e) {
            return false; // token invalid sau expirat
        }
    }

    // extrage username din token
    public String extractUsername(String token) {
        return decodeToken(token).getSubject();
    }

    // verifica daca token-ul a expirat
    public boolean isTokenExpired(String token) {
        Date expiration = decodeToken(token).getExpiresAt();
        return expiration != null && expiration.before(new Date());
    }

    // decodeaza token-ul pentru a extrage informatii
    private DecodedJWT decodeToken(String token) {
        return JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(token);
    }

    public int getExpirationInSeconds() {
        return (int) (jwtExpiration / 1000);
    }

}
