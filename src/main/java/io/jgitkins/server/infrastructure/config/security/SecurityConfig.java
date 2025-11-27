// TODO: Have To Customization
//package io.jgitkins.server.infrastructure.config.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final CustermAuthenticationProvider authProvider;
//
//    @Bean
//    SecurityFilterChain gitChain(HttpSecurity http) throws Exception {
//        // Git Smart HTTP uses POST
//        http.csrf(csrf -> csrf.disable());
//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers("/git/**").permitAll()
//                .anyRequest().permitAll()
//        );
//
//        http.httpBasic(b -> b.realmName("Git"));
//        http.authenticationProvider(authProvider);
//        return http.build();
//    }
//
//    @Bean
//    SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable());
//        http.securityMatcher("/api/**");
//
//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers("/git/**").authenticated()
//                .anyRequest().permitAll()
//        );
//
//        http.httpBasic(b -> b.realmName("Git"));
//        http.authenticationProvider(authProvider);
//
//        return http.build();
//    }
//
//}