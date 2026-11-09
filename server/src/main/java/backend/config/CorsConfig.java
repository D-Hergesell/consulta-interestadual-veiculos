package backend.config;

import backend.controller.VeiculoController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * Libera o client/ (origens em app.cors.allowed-origins) para chamar a API. Usado pelo SecurityConfig.
 * <p>
 * Aceita origem exata (https://meu-front.vercel.app) ou padrao (https://*.vercel.app, util para deploys
 * de preview). Sem cookies nem credenciais, entao o CORS so controla quais sites podem ler as respostas.
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> origensConfiguradas) {
        List<String> origens = origensConfiguradas.stream()
                .map(String::strip)
                .filter(origem -> !origem.isEmpty())
                .toList();
        if (origens.contains("*")) {
            log.warn("CORS_ALLOWED_ORIGINS contem '*': qualquer site pode ler as respostas da API. "
                    + "Em producao, informe o dominio do front.");
        }
        log.info("CORS liberado para: {}", origens);

        CorsConfiguration config = new CorsConfiguration();
        // Padroes aceitam tanto origem exata quanto curinga de subdominio
        config.setAllowedOriginPatterns(origens);
        config.setAllowedMethods(List.of("GET", "OPTIONS"));
        // X-Renavam: prova de posse exigida em /api/veiculos/{placa}/detalhes e /multas
        config.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                VeiculoController.HEADER_RENAVAM));
        // Sem isso o navegador esconde o Retry-After do JavaScript e o front nao sabe quanto esperar apos um 429
        config.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER));
        config.setAllowCredentials(false);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
