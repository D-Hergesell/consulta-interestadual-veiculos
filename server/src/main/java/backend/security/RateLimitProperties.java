package backend.security;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * app.rate-limit.* (ver application.properties). Valor invalido impede a aplicacao de subir.
 *
 * @param enabled                liga/desliga o limite
 * @param requestsPerMinute      requisicoes permitidas por cliente em cada janela de 1 minuto no restante de /api/**
 * @param placaRequestsPerMinute limite proprio (menor) de /api/veiculos/**, alvo de enumeracao de placas
 * @param ipHeader               header com o IP real do cliente, posto pelo proxy (vazio = IP da conexao)
 */
@Validated
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        @Positive int requestsPerMinute,
        @Positive int placaRequestsPerMinute,
        String ipHeader
) {
}
