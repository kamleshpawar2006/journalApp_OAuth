package net.engineeringdigest.journalApp.config;

import net.engineeringdigest.journalApp.filter.JwtFilter;
import net.engineeringdigest.journalApp.security.OAuth2LoginSuccessHandler;
import net.engineeringdigest.journalApp.service.UserDetailsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class BasicSecurityConfig {

    private final UserDetailsImpl userDetailsImpl;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtFilter jwtFilter;

    public BasicSecurityConfig(UserDetailsImpl userDetailsImpl, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler, JwtFilter jwtFilter) {
        this.userDetailsImpl = userDetailsImpl;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/journal/**", "/user/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
