package com.example.Bep_Viet.filter;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                SignedJWT jwt = SignedJWT.parse(token);

                // Verify chữ ký
                MACVerifier verifier = new MACVerifier(signerKey.getBytes());
                if (!jwt.verify(verifier)) {
                    chain.doFilter(request, response);
                    return;
                }

                // Kiểm tra hết hạn
                Date expiry = jwt.getJWTClaimsSet().getExpirationTime();
                if (expiry == null || expiry.before(new Date())) {
                    chain.doFilter(request, response);
                    return;
                }

                // Lấy thông tin từ token
                String role = (String) jwt.getJWTClaimsSet().getClaim("role");
                Long userId = jwt.getJWTClaimsSet().getLongClaim("userId");

                // Set vào SecurityContext
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,  // principal → @AuthenticationPrincipal sẽ lấy được userId
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                log.error("JWT không hợp lệ: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}