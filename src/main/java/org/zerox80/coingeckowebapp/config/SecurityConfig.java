package org.zerox80.coingeckowebapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import reactor.core.publisher.Mono;
import org.zerox80.coingeckowebapp.repository.UserRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> Mono.fromCallable(() -> userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(user -> {
                    if (user == null) {
                        throw new UsernameNotFoundException("User not found");
                    }
                    return org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .roles(user.getRole().replace("ROLE_", ""))
                            .build();
                });
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
        return http
                .authenticationManager(authenticationManager)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .pathMatchers("/portfolio/**").authenticated()
                        .anyExchange().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                )
                .logout(logout -> {
                    RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
                    successHandler.setLogoutSuccessUrl(java.net.URI.create("/login"));
                    logout.logoutUrl("/logout").logoutSuccessHandler(successHandler);
                })
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
                )
                .headers(headers -> headers
                    .frameOptions(options -> options.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN))
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' https://cdn.jsdelivr.net https://stackpath.bootstrapcdn.com; style-src 'self' https://cdn.jsdelivr.net https://stackpath.bootstrapcdn.com 'unsafe-inline'; img-src 'self' data: *.coingecko.com;")
                    )
                )
                .build();
    }
}