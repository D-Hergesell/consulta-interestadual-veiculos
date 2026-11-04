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
│       │   │       │   ├── ConsultaDetalhadaResponse.java   # com RENAVAM: ocorrências, restrições, multas
│       │   │       │   ├── ConsultaPlacaResponse.java       # pública: veículo, situação, furto/roubo
│       │   │       │   ├── EstadoResponse.java
│       │   │       │   ├── InstrucaoTransferenciaResponse.java
│       │   │       │   ├── MultaResponse.java
│       │   │       │   ├── OcorrenciaResponse.java
│       │   │       │   ├── RegraEspecialResponse.java
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
│       │   │       │   ├── ProcedimentoPadraoTransferencia.java   # fallback de transferência
│       │   │       │   ├── Proprietario.java
│       │   │       │   ├── RegraEspecialEstado.java   # regras de transferência por UF
│       │   │       │   ├── Restricao.java
│       │   │       │   ├── Servico.java
│       │   │       │   ├── ServicoEstado.java
│       │   │       │   └── Veiculo.java
│       │   │       ├── enums/
│       │   │       │   ├── EnumPersistivel.java
│       │   │       │   ├── Situacao.java
│       │   │       │   ├── StatusPagamento.java
│       │   │       │   ├── TipoOcorrencia.java
│       │   │       │   ├── TipoPessoa.java
│       │   │       │   └── TipoRegra.java
│       │   │       ├── exception/
│       │   │       │   ├── ApiException.java
│       │   │       │   ├── EstadoNaoEncontradoException.java
│       │   │       │   ├── OrigemDestinoIguaisException.java
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
│       │   │       │   ├── PlacaUtils.java         # normalização/validação de placa (antiga e Mercosul)
│       │   │       │   ├── RenavamUtils.java       # conferência do RENAVAM (prova de posse)
│       │   │       │   └── UrlUtils.java           # só deixa sair URL http/https
│       │   │       └── ConsultaInterestadualVeiculos.java
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/migration/               # scripts Flyway (vazio: banco imutável por enquanto)
│       └── test/
│           └── java/backend/
│               ├── config/SecurityRotasPublicasTest.java   # trava rota sem decisão em ROTAS_PUBLICAS
│               ├── controller/
│               │   ├── TransferenciaControllerTest.java
│               │   ├── VeiculoControllerTest.java
│               │   └── VeiculoProvaDePosseTest.java
│               ├── entities/converter/TipoRegraConverterTest.java
│               ├── mapper/UrlNosMappersTest.java
│               ├── security/
│               │   ├── RateLimitFilterTest.java
│               │   └── RateLimitSecurityChainTest.java
│               ├── services/TransferenciaServiceTest.java
│               └── util/
│                   ├── PlacaUtilsTest.java
│                   ├── RenavamUtilsTest.java
│                   └── UrlUtilsTest.java
└── Estrutura_de_pastas.md
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/veiculos/{placa}` | Consulta pública: veículo, situação, `constaFurtoRoubo` e `possuiRestricaoAtiva` |
| GET | `/api/veiculos/{placa}/detalhes` | Consulta completa (ocorrências com nº do BO, restrições e multas pendentes). Exige header `X-Renavam` |
| GET | `/api/veiculos/{placa}/multas` | Todas as multas do veículo. Exige header `X-Renavam` |
| GET | `/api/estados` | Lista de estados |
| GET | `/api/estados/{uf}/servicos` | Serviços do estado (`tb_servicos_estado`) |
| GET | `/api/servicos` | Catálogo de serviços |
| GET | `/api/transferencias?origem=SC&destino=SP` | Passo a passo, taxa, prazo e documentos. Sem instrução específica para o par, devolve o procedimento padrão nacional (`procedimentoPadrao: true`) com as regras especiais ativas do estado de destino. Origem igual ao destino: `400` |

## Como rodar

1. Copie `server/.env.example` para `server/.env` e preencha `DB_URL` (com `?sslmode=require`), `DB_USERNAME` e `DB_PASSWORD`. Todas as outras configurações do `application.properties` também vêm de variáveis de ambiente e já têm padrão seguro; a lista completa está no `.env.example`.
2. Dentro de `server/`: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Com `SWAGGER_ENABLED=true`, a documentação fica em `http://localhost:8080/swagger-ui.html`.

## Decisões

- **Banco imutável:** `spring.jpa.hibernate.ddl-auto=validate`, e as entidades são `@Immutable`. O Hibernate só confere o mapeamento e nunca altera o schema nem grava dados. O Flyway fica desligado (`FLYWAY_ENABLED=false`).
- **Credenciais:** ficam só em variáveis de ambiente ou em `server/.env`, que é ignorado pelo git.
- **Segurança:** a API é pública e somente leitura. Só são liberadas as rotas GET listadas em `SecurityConfig.ROTAS_PUBLICAS`; todo o resto é negado (`403`), inclusive GET em rota que não está na lista. Endpoint novo precisa entrar na lista de propósito: o `SecurityRotasPublicasTest` falha se existir handler sem essa decisão (ou entrada na lista sem handler). Sem sessão (stateless), sem login padrão do Spring, CORS restrito às origens de `CORS_ALLOWED_ORIGINS` e headers de segurança ativos (HSTS, X-Frame-Options, nosniff, Referrer-Policy). Os erros não expõem stacktrace nem mensagens internas.
- **Rate limit:** janela fixa de 1 minuto por cliente, contado como IPv4 exato ou prefixo `/64` do IPv6 (um provedor entrega um /64 inteiro por assinante). `/api/veiculos/**` tem contador próprio (`RATE_LIMIT_PLACA_REQUESTS_PER_MINUTE`, padrão 20) e o restante de `/api/**` usa `RATE_LIMIT_REQUESTS_PER_MINUTE` (padrão 60). Acima do limite: `429` com `Retry-After`. Os contadores ficam em memória **por instância**: com N réplicas o limite efetivo é N vezes maior (para limite global seria preciso um contador compartilhado, ex.: Bucket4j + Redis).
- **Deploy atrás de proxy:** com `FORWARD_HEADERS_STRATEGY=none` (padrão) o IP vem da conexão, então atrás de um proxy/load balancer todos os clientes dividiriam o IP do proxy e o mesmo limite. Nesse caso use `native` ou `framework`, **mas só** se o proxy sobrescrever o `X-Forwarded-For` recebido do cliente; caso contrário o cliente forja o header e escapa do limite.
- **Saída segura para o front:** `urlDetran` e `urlServico` passam pelo `UrlUtils` e só saem como URL `http`/`https` absoluta (qualquer outra coisa, como `javascript:` ou `data:`, vira `null`), então podem ir direto num `href`. Os textos longos (`passoAPasso`, `documentosNecessarios`, `observacoes`, `descricao`...) são texto puro: o front deve exibi-los como texto, nunca com `v-html`/`dangerouslySetInnerHTML`/`innerHTML`.
- **N+1:** os relacionamentos são `LAZY` e carregados com `@EntityGraph`. `open-in-view` fica desligado.
- **Placa:** normalizada (maiúscula, sem hífen) e validada nos formatos ABC1234 e ABC1D23.
- **Dados pessoais:** nenhum dado do proprietário (nome, CPF/CNPJ) sai pela API. Renavam e chassi também não aparecem nas respostas.
- **Prova de posse:** multas, nº do BO e descrição de restrições só saem com placa + RENAVAM, enviado no header `X-Renavam` (fora da URL, para não ir para logs). RENAVAM ausente ou errado devolve o mesmo 404 de placa inexistente, para não revelar se a placa existe.
- **Multas pendentes:** tudo que não está `Pago`, ou seja, `Pendente`, `Em recurso` e status nulo.
- **Furto/roubo:** `constaFurtoRoubo` é calculado a partir das ocorrências ativas, e não do campo `situacao`. Divergências entre os dois são logadas.
