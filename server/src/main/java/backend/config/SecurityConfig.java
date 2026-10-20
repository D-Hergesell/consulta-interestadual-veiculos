package backend.config;

import backend.security.RateLimitFilter;
import backend.security.RateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;

import java.time.Clock;

/**
 * API publica e somente leitura: libera apenas GET em /api/** (e a documentacao) e nega todo o resto.
 * Quando houver endpoints de escrita/admin, adicionar autenticacao (ex.: JWT) antes de libera-los aqui.
 * Como nao ha login, o rate limit por IP (RateLimitFilter) e a protecao contra abuso e enumeracao de placas.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(RateLimitProperties.class)
public class SecurityConfig {

    private static final String[] DOCUMENTACAO = {"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RateLimitProperties rateLimit,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                // Sem sessao e sem cookies de autenticacao, CSRF nao se aplica
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // Depois do CORS, para o 429 sair com os headers CORS e o front conseguir ler o erro
                .addFilterAfter(new RateLimitFilter(rateLimit, objectMapper, Clock.systemUTC()), CorsFilter.class)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.GET, DOCUMENTACAO).permitAll()
                        // Pagina de erro interna do Spring: sem isso, erros viram 401/403 em vez do status real
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll());

        return http.build();
    }
}
