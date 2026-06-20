package gm.thurstone.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la plataforma:
 *  - El psicólogo se autentica con correo + contraseña (BCrypt) y accede al panel.
 *  - El evaluado NO es un usuario del sistema: entra con la clave de su test
 *    (rutas /acceso, /evaluacion, /resultados quedan públicas y se protegen por
 *    la validación de la clave en sesión, no por autenticación).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y página de inicio.
                        .requestMatchers("/", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        // Login y registro del psicólogo.
                        .requestMatchers("/login", "/registro").permitAll()
                        // Flujo del evaluado (acceso por clave + test), sin login de usuario.
                        .requestMatchers("/acceso", "/evaluacion", "/resultados").permitAll()
                        // Zona del psicólogo.
                        .requestMatchers("/panel/**", "/evaluados/**", "/asignaciones/**", "/evaluaciones/**")
                            .hasRole("PSICOLOGO")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/panel", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }
}
