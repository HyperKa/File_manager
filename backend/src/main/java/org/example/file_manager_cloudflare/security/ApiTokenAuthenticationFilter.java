package org.example.file_manager_cloudflare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final String expectedToken;
    private final SecurityProperties properties;

    public ApiTokenAuthenticationFilter(SecurityProperties properties) {
        this.properties = properties;
        this.expectedToken = properties.getApiToken();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring("Bearer ".length()).trim();
                if (expectedToken != null
                        && messageDigestIsEqual(expectedToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    properties.getDefaultUserId(),
                                    "",
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean messageDigestIsEqual(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            return false;
        }
        int difference = 0;
        for (int i = 0; i < expected.length; i++) {
            difference |= expected[i] ^ actual[i];
        }
        return difference == 0;
    }
}
