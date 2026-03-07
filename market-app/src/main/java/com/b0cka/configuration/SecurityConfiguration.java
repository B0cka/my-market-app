package com.b0cka.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;


@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/cart/**", "/orders/**", "/buy").authenticated()
                        .pathMatchers(HttpMethod.POST, "/items", "/items/**").authenticated()
                        .anyExchange().permitAll()
                )
                .anonymous(Customizer.withDefaults())
                .formLogin(form -> {
                    RedirectServerAuthenticationSuccessHandler successHandler =
                            new RedirectServerAuthenticationSuccessHandler("/items");

                    form.authenticationSuccessHandler(successHandler);
                })
                .csrf(csrf -> csrf.disable())
                .logout(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
