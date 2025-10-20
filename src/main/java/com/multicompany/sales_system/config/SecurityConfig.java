package com.multicompany.sales_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.multicompany.sales_system.security.JwtAuthFilter;
import com.multicompany.sales_system.security.JwtService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(reg -> reg
                // --- RUTAS PÚBLICAS ---
                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/verify-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/resend-code").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/users/check-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/recover-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                // Archivos e imágenes
                .requestMatchers("/api/photos/image/**").permitAll()

                // WebSocket y Chat (para pruebas o funcionalidad pública)
                .requestMatchers("/ws-chat/**", "/ws-chat").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/chat/**").permitAll()

                // Swagger y documentación
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/api-docs/**",
                    "/api-docs",
                    "/actuator/health/**",
                    "/public/**"
                ).permitAll()

                // --- RUTAS PROTEGIDAS POR ROL ---
                .requestMatchers(HttpMethod.POST, "/api/users/admin/**")
                    .hasRole("ADMINISTRADOR")

                // --- EL RESTO REQUIERE AUTENTICACIÓN ---
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    // Nota: la configuración CORS se maneja desde CorsConfig.
}
