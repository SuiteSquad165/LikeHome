package org.suitesquad.likehome;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    /**
     * Configures Spring Security to require JWT authentication for all requests to /auth/**
     */
    @Bean
    @SuppressWarnings("Convert2MethodRef")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a ->
                        a.requestMatchers("/auth/**").authenticated()
//                         .requestMatchers("/admin/**").hasRole("ADMIN")
                         .anyRequest().anonymous())
                .oauth2ResourceServer(o ->
                        o.jwt(Customizer.withDefaults()))
                .csrf(c -> c.disable())
                .build();
    }

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">CORS</a>
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedOrigins("*");
            }
        };
    }
}