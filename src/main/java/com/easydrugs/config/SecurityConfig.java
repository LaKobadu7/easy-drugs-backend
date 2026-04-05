package com.easydrugs.config;

import com.easydrugs.security.JwtAuthenticationFilter;
import com.easydrugs.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

/**
 * Configuration Spring Security + JWT pour EASY-DRUGS.
 * Stateless (pas de session) — authentification par token JWT.
 * 3 rôles : PATIENT, PHARMACIE, ADMIN
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // Active @PreAuthorize sur les controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ── Endpoints publics (aucun token requis) ──────────────────
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**",              // Connexion & inscription
        "/api/medicaments/search",   // Recherche publique médicaments
        "/api/pharmacies/nearby",    // Pharmacies proches (public)
        "/api/pharmacies/garde",     // Pharmacie de garde (public)
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",
        "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (API REST stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Politique CORS (définie dans CorsConfig)
            .cors(cors -> {})
            // Règles d'accès par rôle
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/medicaments/**").permitAll()

                // Endpoints réservés ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/pharmacies").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/pharmacies/**").hasRole("ADMIN")

                // Endpoints réservés PHARMACIE
                .requestMatchers("/api/stocks/**").hasAnyRole("PHARMACIE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/medicaments").hasAnyRole("PHARMACIE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/medicaments/**").hasAnyRole("PHARMACIE", "ADMIN")

                // Endpoints réservés PATIENT
                .requestMatchers("/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/api/ordonnances/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/api/historique/**").hasAnyRole("PATIENT", "ADMIN")

                // Notifications — patient + pharmacie
                .requestMatchers("/api/notifications/**").hasAnyRole("PATIENT", "PHARMACIE", "ADMIN")

                // Litiges — tous les utilisateurs connectés
                .requestMatchers("/api/litiges/**").authenticated()

                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )

            // Session stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Provider d'authentification
            .authenticationProvider(authenticationProvider())

            // Filtre JWT avant le filtre UsernamePassword standard
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
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
        return new BCryptPasswordEncoder(12); // Facteur de coût 12
    }
}
