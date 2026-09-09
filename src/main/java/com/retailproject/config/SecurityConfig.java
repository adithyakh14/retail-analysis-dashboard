package com.retailproject.config;

import com.retailproject.security.ApiTokenAuthenticationFilter;
import com.retailproject.security.CustomAuthenticationFailureHandler;
import com.retailproject.security.SecurityAuditFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            SecurityAuditFilter securityAuditFilter,
            ApiTokenAuthenticationFilter apiTokenAuthenticationFilter,
            CustomAuthenticationFailureHandler failureHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(new OrRequestMatcher(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/api/health"),
                                new AntPathRequestMatcher("/api/**"),
                                new AntPathRequestMatcher("/download/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**"))))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/error/**", "/api/auth/**", "/api/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/download/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/**", "/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/download/**"))
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/v3/api-docs/**")))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard/index.html", true)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true"))
                .httpBasic(httpBasic -> httpBasic.disable())
                .addFilterBefore(apiTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityAuditFilter, org.springframework.security.web.access.intercept.AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        PasswordEncoder delegating = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return delegating.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }
                if (encodedPassword.startsWith("{")) {
                    return delegating.matches(rawPassword, encodedPassword);
                }
                return encodedPassword.contentEquals(rawPassword);
            }
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppSecurityProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedOrigins = properties.allowedOrigins();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/download/**", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        return source;
    }
}
