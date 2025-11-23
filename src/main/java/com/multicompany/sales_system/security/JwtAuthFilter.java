package com.multicompany.sales_system.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔹 Rutas públicas que NO deben pasar por el filtro JWT
        if (path.startsWith("/api/users/login")
                || path.startsWith("/api/users/register")
                || path.startsWith("/api/users/verify-email")
                || path.startsWith("/api/users/resend-code")
                || path.startsWith("/api/users/recover-password")
                || path.startsWith("/api/users/reset-password")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/photos/image")
                || path.startsWith("/actuator/health")
                || path.startsWith("/public")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Leer cabecera Authorization
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // Eliminar "Bearer "
        try {
            if (jwt.isTokenValid(token)) {
                Claims claims = jwt.claims(token);
                String username = claims.getSubject();
                String role = (String) claims.get("role");
                String estado = (String) claims.get("estado");

                // 🔹 Validar estado del usuario
                if ("SUSPENDIDO".equalsIgnoreCase(estado) || "ELIMINADO".equalsIgnoreCase(estado)) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Cuenta " + estado.toLowerCase() + ". No se permite el acceso.\"}");
                    return;
                }

                // 🔹 Asignar autenticación válida al contexto
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
