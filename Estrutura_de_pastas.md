# Estrutura de pastas

Backend: Spring Boot 3.5 (Java 21) + Spring Data JPA + PostgreSQL (Neon) + Spring Security + Lombok, build com Maven.

```text
consulta-interestadual-veiculos/
├── .github/
│   ├── dependabot.yml                          # PRs semanais de atualização (Maven e actions)
│   └── workflows/backend-ci.yml                # testes + build da imagem Docker a cada push/PR
├── client/
├── docs/
│   └── API.md                                  # guia da API para o frontend (rotas, exemplos, erros)
├── server/
│   ├── .mvn/wrapper/maven-wrapper.properties
│   ├── mvnw / mvnw.cmd                         # Maven Wrapper (não precisa de Maven instalado)
│   ├── pom.xml
│   ├── .env.example                            # modelo das variáveis; copiar para server/.env (não commitado)
│   ├── Dockerfile                              # imagem usada no Render (build em 2 estágios, usuário sem root)
│   ├── .dockerignore                           # mantém o .env fora da imagem
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── backend/
│       │   │       ├── config/
│       │   │       │   ├── CorsConfig.java
│       │   │       │   ├── DatabaseUrlEnvironmentPostProcessor.java   # aceita DATABASE_URL do Render
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
│       │       ├── META-INF/spring.factories       # registra o DatabaseUrlEnvironmentPostProcessor
│       │       └── db/migration/               # scripts Flyway (vazio: banco imutável por enquanto)
│       └── test/
│           └── java/backend/
│               ├── config/
│               │   ├── CorsConfigTest.java
│               │   ├── DatabaseUrlEnvironmentPostProcessorTest.java
│               │   └── SecurityRotasPublicasTest.java   # trava rota sem decisão em ROTAS_PUBLICAS
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
├── render.yaml                                 # Blueprint do Render (serviço web Docker)
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
4. Testes: dentro de `server/`, `./mvnw test` (não precisam do `.env` nem acessam o banco). Precisa do JDK 21.

## Integração contínua

- **Backend CI** (`.github/workflows/backend-ci.yml`): a cada push na `main` e em todo pull request que mexa em `server/`, o GitHub instala o Java 21 e roda `./mvnw test` e, em paralelo, constrói a imagem Docker (a mesma do Render). O resultado aparece como ✅/❌ no commit e no PR; se falhar, os relatórios dos testes ficam disponíveis por 7 dias na aba **Actions** (artefato `relatorios-surefire`). Também dá para rodar manualmente pela aba Actions.
- **Dependabot** (`.github/dependabot.yml`): toda segunda-feira abre PRs atualizando as dependências Maven (Spring agrupado num PR só) e as versões das actions. Cada PR passa pela CI antes do merge. Saltos de versão principal do Spring Boot e do springdoc (ex.: 3 → 4) são ignorados de propósito: exigem migração e ficam para uma tarefa própria.

## Frontend

O contrato da API para quem for construir o `client/` está em [docs/API.md](docs/API.md): rotas, exemplos de resposta, valores possíveis, erros e como enviar o RENAVAM. Do lado do backend, a conexão com o front depende só de `CORS_ALLOWED_ORIGINS` (domínios do front, aceita curinga como `https://*.vercel.app`). O backend já expõe o `Retry-After` para o front ler no `429`.

## Deploy no Render

O Render não tem runtime nativo de Java, então o backend sobe como **Docker** (`server/Dockerfile`), descrito no Blueprint `render.yaml`.

1. No Render: **New → Blueprint** e escolha este repositório. Ele cria o serviço web `consulta-interestadual-veiculos-api` (Docker, região Virginia, plano free) e pede as variáveis marcadas com `sync: false`.
2. **Banco**, escolha uma opção:
   - **Neon (atual):** preencha `DB_URL` (`jdbc:postgresql://<host>/<banco>?sslmode=require`), `DB_USERNAME` e `DB_PASSWORD`.
   - **Postgres do Render:** apague as três e crie `DATABASE_URL` com a **Internal Database URL** do banco (formato `postgres://...`). O backend converte sozinho na inicialização.
3. `CORS_ALLOWED_ORIGINS`: o domínio do front (pode ficar `http://localhost:5173` até o front existir).
4. O health check é `GET /actuator/health/readiness`; o deploy só troca de versão quando a nova instância responde `200`.
5. **IP do cliente para o rate limit** (fazer no primeiro deploy):
   1. Adicione temporariamente `LOGGING_LEVEL_BACKEND_SECURITY=DEBUG`.
   2. Acesse qualquer rota `/api/...` e veja nos logs do Render a linha `Origem do IP: remoteAddr=... | X-Forwarded-For=... | CF-Connecting-IP=... | True-Client-IP=...`.
   3. Se `remoteAddr` for um IP interno (ex.: `10.x`) e `CF-Connecting-IP` (ou `True-Client-IP`) trouxer o seu IP público, defina `RATE_LIMIT_IP_HEADER` com o nome desse header. Confirme mandando você mesmo um `CF-Connecting-IP: 1.2.3.4` (ex.: `curl -H "CF-Connecting-IP: 1.2.3.4" ...`): se o log continuar mostrando o seu IP real, o proxy sobrescreve o header e ele é confiável; se mostrar `1.2.3.4`, **não use** esse header.
   4. Remova o `LOGGING_LEVEL_BACKEND_SECURITY`.
6. Mantenha **uma instância** (o rate limit é em memória, por instância).

Observações:
- O Neon está em São Paulo (`sa-east-1`) e o Render não tem região na América do Sul; Virginia é a mais próxima (~120 ms por ida e volta ao banco). Um Postgres do Render na mesma região elimina essa latência.
- No plano free o serviço hiberna após um período sem acesso; a primeira requisição depois disso pode levar cerca de 1 minuto.
- Rodar a imagem localmente: `docker build -t consulta-api server` e `docker run --rm -p 8080:8080 --env-file server/.env consulta-api`.

## Decisões

- **Banco imutável:** `spring.jpa.hibernate.ddl-auto=validate`, e as entidades são `@Immutable`. O Hibernate só confere o mapeamento e nunca altera o schema nem grava dados. O Flyway fica desligado (`FLYWAY_ENABLED=false`).
- **Credenciais:** ficam só em variáveis de ambiente ou em `server/.env`, que é ignorado pelo git.
- **Segurança:** a API é pública e somente leitura. Só são liberadas as rotas GET listadas em `SecurityConfig.ROTAS_PUBLICAS`; todo o resto é negado (`403`), inclusive GET em rota que não está na lista. Endpoint novo precisa entrar na lista de propósito: o `SecurityRotasPublicasTest` falha se existir handler sem essa decisão (ou entrada na lista sem handler). Sem sessão (stateless), sem login padrão do Spring, CORS restrito às origens de `CORS_ALLOWED_ORIGINS` e headers de segurança ativos (HSTS, X-Frame-Options, nosniff, Referrer-Policy). Os erros não expõem stacktrace nem mensagens internas.
- **Rate limit:** janela fixa de 1 minuto por cliente, contado como IPv4 exato ou prefixo `/64` do IPv6 (um provedor entrega um /64 inteiro por assinante). `/api/veiculos/**` tem contador próprio (`RATE_LIMIT_PLACA_REQUESTS_PER_MINUTE`, padrão 20) e o restante de `/api/**` usa `RATE_LIMIT_REQUESTS_PER_MINUTE` (padrão 60). Acima do limite: `429` com `Retry-After`. Os contadores ficam em memória **por instância**: com N réplicas o limite efetivo é N vezes maior (para limite global seria preciso um contador compartilhado, ex.: Bucket4j + Redis).
- **IP do cliente atrás de proxy:** por padrão o rate limit usa o IP da conexão. Atrás de um proxy (Render) todos os clientes chegariam com o IP do proxy e dividiriam o mesmo limite; por isso `RATE_LIMIT_IP_HEADER` aponta um header que o proxy **sobrescreve** com o IP real (ver Deploy no Render). `FORWARD_HEADERS_STRATEGY` fica `none`: no Render o proxy só acrescenta ao `X-Forwarded-For` enviado pelo cliente, que poderia forjar o próprio IP.
- **Saída segura para o front:** `urlDetran` e `urlServico` passam pelo `UrlUtils` e só saem como URL `http`/`https` absoluta (qualquer outra coisa, como `javascript:` ou `data:`, vira `null`), então podem ir direto num `href`. Os textos longos (`passoAPasso`, `documentosNecessarios`, `observacoes`, `descricao`...) são texto puro: o front deve exibi-los como texto, nunca com `v-html`/`dangerouslySetInnerHTML`/`innerHTML`.
- **N+1:** os relacionamentos são `LAZY` e carregados com `@EntityGraph`. `open-in-view` fica desligado.
- **Placa:** normalizada (maiúscula, sem hífen) e validada nos formatos ABC1234 e ABC1D23.
- **Dados pessoais:** nenhum dado do proprietário (nome, CPF/CNPJ) sai pela API. Renavam e chassi também não aparecem nas respostas.
- **Prova de posse:** multas, nº do BO e descrição de restrições só saem com placa + RENAVAM, enviado no header `X-Renavam` (fora da URL, para não ir para logs). RENAVAM ausente ou errado devolve o mesmo 404 de placa inexistente, para não revelar se a placa existe.
- **Multas pendentes:** tudo que não está `Pago`, ou seja, `Pendente`, `Em recurso` e status nulo.
- **Furto/roubo:** `constaFurtoRoubo` é calculado a partir das ocorrências ativas, e não do campo `situacao`. Divergências entre os dois são logadas.
