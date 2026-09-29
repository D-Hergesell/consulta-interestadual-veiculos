# Estrutura de pastas

Backend: Spring Boot 3.5 (Java 21) + Spring Data JPA + PostgreSQL (Neon) + Spring Security + Lombok, build com Maven.

```text
consulta-interestadual-veiculos/
├── client/
├── server/
│   ├── .mvn/wrapper/maven-wrapper.properties
│   ├── mvnw / mvnw.cmd                         # Maven Wrapper (não precisa de Maven instalado)
│   ├── pom.xml
│   ├── .env.example                            # modelo das variáveis; copiar para server/.env (não commitado)
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── backend/
│       │   │       ├── config/
│       │   │       │   ├── CorsConfig.java
│       │   │       │   ├── OpenApiConfig.java
│       │   │       │   └── SecurityConfig.java
│       │   │       ├── controller/
│       │   │       │   ├── EstadoController.java
│       │   │       │   ├── ServicoController.java
│       │   │       │   ├── TransferenciaController.java
│       │   │       │   └── VeiculoController.java
│       │   │       ├── dto/
│       │   │       │   ├── ConsultaPlacaResponse.java
│       │   │       │   ├── EstadoResponse.java
│       │   │       │   ├── InstrucaoTransferenciaResponse.java
│       │   │       │   ├── MultaResponse.java
│       │   │       │   ├── OcorrenciaResponse.java
│       │   │       │   ├── ProprietarioResponse.java
│       │   │       │   ├── RestricaoResponse.java
│       │   │       │   ├── ServicoEstadoResponse.java
│       │   │       │   ├── ServicoResponse.java
│       │   │       │   └── VeiculoResponse.java
│       │   │       ├── entities/
│       │   │       │   ├── converter/              # AttributeConverters dos enums (valor gravado no banco)
│       │   │       │   ├── CategoriaServico.java
│       │   │       │   ├── Estado.java
│       │   │       │   ├── HistoricoProprietario.java
│       │   │       │   ├── InstrucaoTransferencia.java
│       │   │       │   ├── Multa.java
│       │   │       │   ├── OcorrenciaFurtoRoubo.java
│       │   │       │   ├── Proprietario.java
│       │   │       │   ├── Restricao.java
│       │   │       │   ├── Servico.java
│       │   │       │   ├── ServicoEstado.java
│       │   │       │   └── Veiculo.java
│       │   │       ├── enums/
│       │   │       │   ├── EnumPersistivel.java
│       │   │       │   ├── Situacao.java
│       │   │       │   ├── StatusPagamento.java
│       │   │       │   ├── TipoOcorrencia.java
│       │   │       │   └── TipoPessoa.java
│       │   │       ├── exception/
│       │   │       │   ├── ApiException.java
│       │   │       │   ├── EstadoNaoEncontradoException.java
│       │   │       │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice (ProblemDetail / RFC 7807)
│       │   │       │   ├── PlacaInvalidaException.java
│       │   │       │   ├── PlacaNaoEncontradaException.java
│       │   │       │   └── RegraTransferenciaNaoEncontradaException.java
│       │   │       ├── mapper/                 # entidade -> DTO (manual)
│       │   │       │   ├── EstadoMapper.java
│       │   │       │   ├── ServicoMapper.java
│       │   │       │   ├── TransferenciaMapper.java
│       │   │       │   └── VeiculoMapper.java
│       │   │       ├── repository/             # um JpaRepository por entidade
│       │   │       ├── services/
│       │   │       │   ├── EstadoService.java
│       │   │       │   ├── ServicoService.java
│       │   │       │   ├── TransferenciaService.java
│       │   │       │   └── VeiculoService.java
│       │   │       ├── util/
│       │   │       │   ├── DocumentoUtils.java     # máscara de CPF/CNPJ
│       │   │       │   └── PlacaUtils.java         # normalização/validação de placa (antiga e Mercosul)
│       │   │       └── ConsultaInterestadualVeiculos.java
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/migration/               # scripts Flyway (vazio: banco imutável por enquanto)
│       └── test/
│           └── java/backend/
│               ├── controller/VeiculoControllerTest.java
│               └── util/
│                   ├── DocumentoUtilsTest.java
│                   └── PlacaUtilsTest.java
└── Estrutura_de_pastas.md
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/veiculos/{placa}` | Consulta completa: veículo, situação, furto/roubo, restrições e multas pendentes, proprietário atual |
| GET | `/api/veiculos/{placa}/multas` | Todas as multas do veículo |
| GET | `/api/estados` | Lista de estados |
| GET | `/api/estados/{uf}/servicos` | Serviços do estado (`tb_servicos_estado`) |
| GET | `/api/servicos` | Catálogo de serviços |
| GET | `/api/transferencias?origem=SC&destino=SP` | Passo a passo, taxa, prazo e documentos |

## Como rodar

1. Copie `server/.env.example` para `server/.env` e preencha `DB_URL` (com `?sslmode=require`), `DB_USERNAME` e `DB_PASSWORD`. Todas as outras configurações do `application.properties` também vêm de variáveis de ambiente e já têm padrão seguro; a lista completa está no `.env.example`.
2. Dentro de `server/`: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Com `SWAGGER_ENABLED=true`, a documentação fica em `http://localhost:8080/swagger-ui.html`.

## Decisões

- **Banco imutável:** `spring.jpa.hibernate.ddl-auto=validate`, e as entidades são `@Immutable`. O Hibernate só confere o mapeamento e nunca altera o schema nem grava dados. O Flyway fica desligado (`FLYWAY_ENABLED=false`).
- **Credenciais:** ficam só em variáveis de ambiente ou em `server/.env`, que é ignorado pelo git.
- **Segurança:** a API é pública e somente leitura. Só `GET /api/**` é liberado e todo o resto é negado. Sem sessão (stateless), sem login padrão do Spring, CORS restrito às origens de `CORS_ALLOWED_ORIGINS` e headers de segurança ativos (HSTS, X-Frame-Options, nosniff, Referrer-Policy). Os erros não expõem stacktrace nem mensagens internas.
- **N+1:** os relacionamentos são `LAZY` e carregados com `@EntityGraph`. `open-in-view` fica desligado.
- **Placa:** normalizada (maiúscula, sem hífen) e validada nos formatos ABC1234 e ABC1D23.
- **Dados pessoais:** CPF/CNPJ sempre mascarado no DTO (`***.456.789-**`).
- **Multas pendentes:** tudo que não está `Pago`, ou seja, `Pendente`, `Em recurso` e status nulo.
- **Furto/roubo:** `constaFurtoRoubo` é calculado a partir das ocorrências ativas, e não do campo `situacao`. Divergências entre os dois são logadas.
