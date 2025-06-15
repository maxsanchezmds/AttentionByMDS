package com.attention.analysis.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Rutas públicas
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/webhook/**").permitAll()
                        .pathMatchers("/wsp/**").permitAll()
                        .pathMatchers("/api/messages/webhook/**").permitAll()
                        .pathMatchers("/executive/**").permitAll()
                        .pathMatchers("/socket.io/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/actuator/health/**").permitAll()
                        // Todas las demás rutas requieren autenticación (manejado por el filtro personalizado)
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes específicos (ajustar según tu frontend)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales (cookies)
        configuration.setAllowCredentials(true);
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-User-Email"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}