package app.control.config;

import app.control.repository.EmpleadoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    private final EmpleadoRepository empleadoRepository;

    public WebSecurityConfig(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/fichaje").authenticated()
                .anyRequest().permitAll()
                )
                .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/api/login")
                .successHandler((request, response, authentication) -> {
                    var roles = authentication.getAuthorities();
                    if (roles.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/fichaje");
                    }
                })
                .failureUrl("/?error")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                )
                .exceptionHandling(ex -> ex
                .accessDeniedPage("/")
                )
                .requestCache(requestCache -> requestCache.disable()); // 🔹 evita redirección a index.html

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var empleadoOpt = empleadoRepository.findByUsuario(username);
            if (empleadoOpt.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }
            var empleado = empleadoOpt.get();
            return User.builder()
                    .username(empleado.getUsuario())
                    .password(empleado.getContraseña())
                    .roles(empleado.getRol())
                    .build();
        };
    }

    @Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
