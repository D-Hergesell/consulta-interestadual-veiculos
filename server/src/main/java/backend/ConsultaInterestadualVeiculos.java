package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration e excluida para o Spring nao gerar (e logar) um usuario/senha padrao:
// a API e somente leitura e publica, sem login.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ConsultaInterestadualVeiculos {

    public static void main(String[] args) {
        SpringApplication.run(ConsultaInterestadualVeiculos.class, args);
    }
}
