package backend.config;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Aceita a conexao do banco no formato que o Render (e o Heroku) entrega: uma unica variavel
 * DATABASE_URL=postgres://usuario:senha@host:porta/banco?parametros.
 * <p>
 * So age quando DB_URL NAO esta definida: ai converte DATABASE_URL para spring.datasource.url (JDBC),
 * username e password. Com DB_URL definida (ex.: Neon), nada muda. Registrado em META-INF/spring.factories.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE = "databaseUrl";

    private final Log log;

    public DatabaseUrlEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(DatabaseUrlEnvironmentPostProcessor.class);
    }

    /** Depois do processamento de config data, para enxergar tambem o DATABASE_URL vindo do server/.env. */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank() || environment.containsProperty("DB_URL")) {
            return;
        }
        Conexao conexao = converter(databaseUrl);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, Map.of(
                "spring.datasource.url", conexao.jdbcUrl(),
                "spring.datasource.username", conexao.usuario(),
                "spring.datasource.password", conexao.senha())));
        // Nunca loga usuario nem senha
        log.info("Conexao com o banco lida de DATABASE_URL (host " + conexao.host() + ")");
    }

    /**
     * Converte postgres://usuario:senha@host:porta/banco?params em jdbc:postgresql://host:porta/banco?params.
     *
     * @throws IllegalStateException se a URL nao estiver nesse formato (a aplicacao nao sobe)
     */
    static Conexao converter(String databaseUrl) {
        URI uri;
        try {
            uri = new URI(databaseUrl.strip());
        } catch (URISyntaxException e) {
            throw invalida();
        }
        String esquema = uri.getScheme();
        String userInfo = uri.getRawUserInfo();
        String banco = uri.getRawPath();
        if (esquema == null || !(esquema.equals("postgres") || esquema.equals("postgresql"))
                || uri.getHost() == null || userInfo == null || !userInfo.contains(":")
                || banco == null || banco.length() <= 1) {
            throw invalida();
        }
        int separador = userInfo.indexOf(':');
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                + banco
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        return new Conexao(jdbcUrl, uri.getHost(),
                decodificar(userInfo.substring(0, separador)), decodificar(userInfo.substring(separador + 1)));
    }

    /** Decodifica %XX sem transformar '+' em espaco (senha pode ter '+'). */
    private static String decodificar(String valor) {
        return URLDecoder.decode(valor.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    // A mensagem nunca inclui a URL, que contem a senha
    private static IllegalStateException invalida() {
        return new IllegalStateException(
                "DATABASE_URL invalida: use postgres://usuario:senha@host:porta/banco (ou defina DB_URL, DB_USERNAME e DB_PASSWORD)");
    }

    record Conexao(String jdbcUrl, String host, String usuario, String senha) {

        // Sem a senha no toString (evita vazar em log ou mensagem de erro)
        @Override
        public String toString() {
            return "Conexao[" + jdbcUrl + "]";
        }
    }
}
