package backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI em /swagger-ui.html, habilitado so quando SWAGGER_ENABLED=true.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Consulta Interestadual de Veículos")
                .description("Consulta de placas, serviços por estado e regras de transferência entre UFs.")
                .version("v1"));
    }
}
