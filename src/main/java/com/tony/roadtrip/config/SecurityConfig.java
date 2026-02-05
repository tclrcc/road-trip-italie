package com.tony.roadtrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests((requests) -> requests
                        // Autoriser les ressources statiques (CSS, JS) et la console H2
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/h2-console/**", "/favicon.ico").permitAll()
                        // tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .permitAll() // Affiche la page de login par défaut de Spring
                        .defaultSuccessUrl("/", true) // Redirige vers l'accueil après login
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout") // Redirige vers login après déconnexion
                        .permitAll()
                );

        // --- FIX POUR LA CONSOLE H2 ---
        // La console H2 utilise des "frames" que Spring Security bloque par défaut.
        // On doit aussi désactiver la protection CSRF uniquement pour la console.
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Définition des utilisateurs en mémoire
        // Pour une vraie prod publique, on chiffrerait les mots de passe (BCrypt)
        // Ici, "{noop}" dit à Spring "ne chiffre pas, c'est du texte clair" (OK pour ton usage)

        UserDetails tony = User.withUsername("tony")
                .password("{noop}admin") // Change le mot de passe ici !
                .roles("ADMIN")
                .build();

        UserDetails jade = User.withUsername("jade")
                .password("{noop}admin")  // Change le mot de passe ici !
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(tony, jade);
    }
}
