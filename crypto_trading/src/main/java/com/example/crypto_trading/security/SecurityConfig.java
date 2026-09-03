package com.example.crypto_trading.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì dùng JWT stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Session: cho phép tạo session khi cần (WebSocket handshake yêu cầu)
                // JWT vẫn được kiểm tra độc lập trên mỗi request REST
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public: auth endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register/**"
                        ).permitAll()
                        // Public: market data (không cần đăng nhập)
                        .requestMatchers("/api/binance/**").permitAll()
                        // Public: WebSocket STOMP handshake
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        // Mọi endpoint khác yêu cầu xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
