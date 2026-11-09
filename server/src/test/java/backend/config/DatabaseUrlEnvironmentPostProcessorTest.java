package backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor =
            new DatabaseUrlEnvironmentPostProcessor(new DeferredLogs());

    @Test
    void converteUrlDoRenderParaJdbc() {
        var conexao = DatabaseUrlEnvironmentPostProcessor.converter(
                "postgres://app_user:s3nh4@dpg-abc123-a.virginia-postgres.render.com:5432/consulta?sslmode=require");

        assertThat(conexao.jdbcUrl())
                .isEqualTo("jdbc:postgresql://dpg-abc123-a.virginia-postgres.render.com:5432/consulta?sslmode=require");
        assertThat(conexao.usuario()).isEqualTo("app_user");
        assertThat(conexao.senha()).isEqualTo("s3nh4");
    }

    @Test
    void aceitaUrlInternaSemPortaEEsquemaPostgresql() {
        // URL interna do Render (mesma regiao) costuma vir sem porta e sem sslmode
        var conexao = DatabaseUrlEnvironmentPostProcessor.converter("postgresql://u:p@dpg-abc123-a/consulta");

        assertThat(conexao.jdbcUrl()).isEqualTo("jdbc:postgresql://dpg-abc123-a/consulta");
    }

    @Test
    void decodificaSenhaComCaracteresEspeciaisSemTrocarMaisPorEspaco() {
        var conexao = DatabaseUrlEnvironmentPostProcessor.converter("postgres://u:a%40b%3Ac+d@host/db");

        assertThat(conexao.senha()).isEqualTo("a@b:c+d");
    }

    @Test
    void urlInvalidaFalhaSemExporASenha() {
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.converter("mysql://u:segredo@host/db"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("segredo");
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.converter("postgres://host/db"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.converter("postgres://u:p@host/"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void toStringDaConexaoNaoMostraASenha() {
        var conexao = DatabaseUrlEnvironmentPostProcessor.converter("postgres://u:segredo@host/db");

        assertThat(conexao.toString()).doesNotContain("segredo");
    }

    @Test
    void comSoDatabaseUrlPreencheODatasource() {
        MockEnvironment env = new MockEnvironment().withProperty("DATABASE_URL", "postgres://u:p@host:5432/db");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://host:5432/db");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("u");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("p");
    }

    @Test
    void comDbUrlDefinidaNaoMudaNada() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "jdbc:postgresql://neon/db")
                .withProperty("DATABASE_URL", "postgres://u:p@host/db");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getPropertySources().contains(DatabaseUrlEnvironmentPostProcessor.PROPERTY_SOURCE)).isFalse();
    }

    @Test
    void semDatabaseUrlNaoFazNada() {
        MockEnvironment env = new MockEnvironment();

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getPropertySources().contains(DatabaseUrlEnvironmentPostProcessor.PROPERTY_SOURCE)).isFalse();
    }
}
