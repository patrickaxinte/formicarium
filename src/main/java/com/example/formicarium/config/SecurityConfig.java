//package com.example.formicarium.config;
//
//import com.example.formicarium.service.UserDetailsServiceImpl;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import jakarta.servlet.http.HttpServletResponse;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final UserDetailsService userDetailsService;
//
//    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable()) // Dezactivăm temporar CSRF pentru debugging
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers("/api/register", "/images/**").permitAll()
////                        .anyRequest().authenticated()
////                )
////                .httpBasic(httpBasic -> httpBasic
////                        .authenticationEntryPoint((request, response, authException) -> {
////                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
////                        })
////                )
////                .logout(logout -> logout
////                        .logoutRequestMatcher(org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/logout"))
////                        .logoutSuccessHandler((request, response, authentication) -> {
////                            response.sendRedirect("/login?logout");
////                        })
////                        .invalidateHttpSession(true)
////                        .deleteCookies("JSESSIONID")
////                        .clearAuthentication(true)
////                );
////
////        return http.build();
////    }
//
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Dezactivăm temporar CSRF pentru debugging
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/login", "/register", "/api/register", "/images/**", "/css/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                // Dezactivăm httpBasic pentru endpoint-urile publice
//                .httpBasic(httpBasic -> httpBasic
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            // Dacă cererea este către un endpoint public, nu cere autentificare
//                            if (!request.getRequestURI().startsWith("/api/")) {
//                                response.sendRedirect("/login");
//                            } else {
//                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//                            }
//                        })
//                )
//                .logout(logout -> logout
//                        .logoutRequestMatcher(org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/logout"))
//                        .logoutSuccessUrl("/login?logout")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .clearAuthentication(true)
//                        .permitAll()
//                );
//
//        return http.build();
//    }
//
//
//}


package com.example.formicarium.config;

import com.example.formicarium.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Eliminăm AuthenticationManager dacă nu este necesar
    // @Bean
    // public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    //     return config.getAuthenticationManager();
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/images/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/register")
                );

        return http.build();
    }
}
