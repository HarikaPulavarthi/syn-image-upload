package com.synchrony.cloudinary.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API use cases
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/h2-console/**", "/api/users/v1/register", "/api/users/v1/login").permitAll()  // Allow public endpoints
                            .anyRequest().authenticated()               // All other endpoints require authentication
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()) // Use JWT for OAuth2 token validation
                    ).headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable());

            return http.build();
        }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
