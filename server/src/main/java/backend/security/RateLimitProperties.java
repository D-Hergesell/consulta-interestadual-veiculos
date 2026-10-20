package backend.security;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * app.rate-limit.* (ver application.properties). Valor invalido impede a aplicacao de subir.
 *
 * @param enabled           liga/desliga o limite
 * @param requestsPerMinute requisicoes permitidas por IP em cada janela de 1 minuto
 */
@Validated
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        @Positive int requestsPerMinute
) {
}
