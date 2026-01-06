package com.demo.util;

import com.demo.exception.user.JWTTokenException;
import com.demo.pojo.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;


public class JWTUtil {

    private static final String SECRET = "1342F899F1019RD244VD471BD454FFD2";
    public static final Long KEEPALIVE_TIME = 30L * 24 * 60 * 60 * 1000;
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));


    public static String generateJWT(User user) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", user.getId())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + KEEPALIVE_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long parseJWT(String token) {
        try {
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build();

            Claims payload = jwtParser.parseClaimsJws(token).getBody();

            if (payload.getExpiration().before(new Date())) {
                return null;
            }

            return payload.get("userId", Long.class);
        } catch (ExpiredJwtException ignore) {

        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new JWTTokenException();
        }
        return null;
    }

    public static Date parseExpiration(String token) {
        Claims payload = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return payload.getExpiration();
    }

    public static String parseJti(String token) {
        Claims payload = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return payload.getId();
    }


}
