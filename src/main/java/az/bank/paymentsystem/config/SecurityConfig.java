package az.bank.paymentsystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import az.bank.paymentsystem.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/api/v1/auth/create-admin")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/create")
                        .hasRole("ADMIN") // ✅ добавили!
                        .requestMatchers("/api/v1/customers/getAll")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/block/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/unblock/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/delete/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/cards/*/unblock")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/current-accounts/*/unblock")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/getById/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/customers/update/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/card-orders/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/cards/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/account-order/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/current-accounts/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/payments/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/transfers/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/transactions/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/currency/**")
                        .hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
