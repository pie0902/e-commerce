package org.example.share.config.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.example.share.config.global.entity.user.UserRoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JwtUtil (overrides shared jar to remove fixed Domain on cookie)
 */
@Slf4j
@Component
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.key}")
    private String secretKey;

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String resolveToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        return null;
    }

    public void addJwtToCookie(String token, HttpServletResponse res) {
        token = URLEncoder.encode(token, StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");

        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 hour
        res.addCookie(cookie);
    }

    // Signature used by microservices' controllers
    public void removeJwtAtCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    // Overload kept for compatibility with monolithic usage
    public void removeJwtAtCookie(HttpServletRequest request, HttpServletResponse res) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
        for (Cookie loginCookie : cookies) {
            if (AUTHORIZATION_HEADER.equals(loginCookie.getName())) {
                loginCookie.setMaxAge(0);
                loginCookie.setPath("/");
                res.addCookie(loginCookie);
            }
        }
    }

    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Signature used by current microservices (id, email, username, role)
    public String createToken(Long userId, String email, String username, UserRoleEnum role) {
        return doCreateToken(userId, username, email, role);
    }

    private String doCreateToken(Long userId, String username, String email, UserRoleEnum role) {
        Date date = new Date();
        long TOKEN_TIME = 60 * 60 * 1000; // 60 min
        String compact = Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("username", username)
            .claim("email", email)
            .claim("role", role)
            .setExpiration(new Date(date.getTime() + TOKEN_TIME))
            .setIssuedAt(date)
            .signWith(key, signatureAlgorithm)
            .compact();
        return BEARER_PREFIX + compact;
    }

    public String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AUTHORIZATION_HEADER.equals(cookie.getName())) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
