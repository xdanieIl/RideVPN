package com.ridevpn.ridevpn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/error", "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            // The desktop client sends its username and password on every request (no cookie),
            // so it can't be hit by CSRF. Only browser requests need the CSRF token.
            .csrf(csrf -> csrf.ignoringRequestMatchers(request -> {
                String auth = request.getHeader("Authorization");
                return auth != null && auth.startsWith("Basic ");
            }))
            .formLogin(form -> form.loginPage("/").loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true).failureUrl("/?error").permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?logout").permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
