package ru.angelika.boutique.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security.
 * Определяет правила доступа к URL, настройки формы логина,
 * обработку отказа в доступе и отключает CSRF для упрощения.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    /**
     * Бин кодировщика паролей с использованием алгоритма BCrypt.
     *
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает цепочку фильтров безопасности.
     * Определяет:
     * <ul>
     *   <li>Публичные страницы: /login, /registration</li>
     *   <li>Доступ только для ADMIN: /admin/**, /swagger-ui/**, /v3/api-docs/**, /monitoring/**, /actuator/**</li>
     *   <li>Доступ для SELLER и ADMIN: /sellers/**, /items/**, /cards/**, /send-to-storage/**</li>
     *   <li>Доступ только для SELLER: /seller/**</li>
     *   <li>Доступ для USER и ADMIN: /users/**</li>
     *   <li>Доступ только для USER: /user/**</li>
     *   <li>Остальные запросы требуют аутентификации</li>
     * </ul>
     * <p>Настройка формы логина: номер телефона как username, перенаправление на /welcome.</p>
     * <p>При отказе в доступе показывается страница /access-denied.</p>
     * <p>CSRF отключён.</p>
     *
     * @param http объект HttpSecurity для конфигурации
     * @return настроенный SecurityFilterChain
     * @throws Exception если конфигурация не может быть применена
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login", "/registration").permitAll()
                        .requestMatchers("/admin/**", "/swagger-ui/**", "/v3/api-docs/**", "/monitoring/**", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/sellers/**", "/items/**", "/cards/**", "/send-to-storage/**")
                        .hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/seller/**").hasRole("SELLER")
                        .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("number")   // используется номер телефона вместо стандартного "username"
                        .defaultSuccessUrl("/welcome", true)
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                )
                .csrf(AbstractHttpConfigurer::disable); // CSRF отключён
        return http.build();
    }
}