# API — guia para o frontend

Contrato do backend (`server/`) para quem vai construir o `client/`. Tudo é **GET**, público e somente leitura; respostas em JSON (UTF-8).

## Endereço base

| Ambiente | URL base |
|---|---|
| Local | `http://localhost:8080` |
| Render | `https://<nome-do-servico>.onrender.com` (definido no deploy) |

Sugestão no front: guardar a URL numa variável de ambiente do build (ex.: `VITE_API_URL`) em vez de deixá-la fixa no código.

## CORS (o que o backend precisa saber do front)

O navegador só deixa o front ler as respostas se a origem dele estiver em `CORS_ALLOWED_ORIGINS` no backend:

- local (Vite): `http://localhost:5173` (já é o padrão)
- produção: o domínio do front, ex.: `https://consulta-veiculos.vercel.app`
- previews: curinga de subdomínio, ex.: `https://*.vercel.app`

Origem fora da lista recebe `403`. Ao publicar o front, avise quem cuida do backend para adicionar o domínio. Não há cookies nem login: **não** use `credentials: "include"`.

## Rotas

| Rota | Precisa de RENAVAM | Resposta |
|---|---|---|
| `GET /api/veiculos/{placa}` | não | dados públicos do veículo |
| `GET /api/veiculos/{placa}/detalhes` | **sim** | ocorrências, restrições e multas pendentes |
| `GET /api/veiculos/{placa}/multas` | **sim** | todas as multas (inclusive pagas) |
| `GET /api/estados` | não | Detran de cada UF (site, telefone, WhatsApp) |
| `GET /api/estados/{uf}/servicos` | não | serviços do Detran da UF, com taxa e prazo |
| `GET /api/servicos` | não | catálogo geral de serviços |
| `GET /api/transferencias?origem=SC&destino=SP` | não | passo a passo da transferência entre UFs |

- **Placa:** aceita `ABC1234` ou `ABC1D23`, com ou sem hífen, maiúscula ou minúscula.
- **UF:** duas letras (`sc` ou `SC`).

### RENAVAM (prova de posse)

`/detalhes` e `/multas` exigem o RENAVAM do veículo no **cabeçalho** `X-Renavam` (nunca na URL, para não ir para logs e histórico):

```js
const resp = await fetch(`${API}/api/veiculos/${placa}/detalhes`, {
  headers: { "X-Renavam": renavam },
});
```

RENAVAM ausente ou errado devolve **o mesmo 404 de placa inexistente**, de propósito (não revela se a placa existe). Na tela, trate os três casos com uma mensagem só, ex.: "Veículo não encontrado. Confira a placa e o RENAVAM."

## Exemplos de resposta

Os valores abaixo são ilustrativos; os nomes dos campos e os tipos são os reais.

`GET /api/veiculos/ABC1D23`

```json
{
  "veiculo": {
    "placa": "ABC1D23", "marca": "Fiat", "modelo": "Argo", "anoFabricacao": 2021, "anoModelo": 2022,
    "cor": "Prata", "tipoVeiculo": "Automóvel", "combustivel": "Flex", "categoria": "Particular",
    "ufRegistro": "SC", "municipioRegistro": "Florianópolis"
  },
  "situacao": "REGULAR",
  "constaFurtoRoubo": false,
  "possuiRestricaoAtiva": true
}
```

`GET /api/veiculos/ABC1D23/detalhes` (com `X-Renavam`)

```json
{
  "veiculo": { "...": "mesmo objeto da consulta pública" },
  "situacao": "REGULAR",
  "constaFurtoRoubo": false,
  "ocorrenciasAtivas": [
    { "tipoOcorrencia": "FURTO", "dataOcorrencia": "2025-03-14", "ufOcorrencia": "SP",
      "municipioOcorrencia": "Campinas", "boletimOcorrencia": "123456/2025", "orgaoRegistro": "PC-SP" }
  ],
  "restricoesAtivas": [
    { "tipoRestricao": "Alienação Fiduciária", "descricao": "Financiamento ativo", "orgaoResponsavel": "Banco X",
      "dataInclusao": "2024-08-01" }
  ],
  "multasPendentes": [
    { "dataInfracao": "2025-01-20", "codigoInfracao": "74550", "descricao": "Excesso de velocidade até 20%",
      "valor": 130.16, "pontos": 4, "statusPagamento": "PENDENTE" }
  ],
  "totalMultasPendentes": 130.16
}
```

`GET /api/transferencias?origem=RJ&destino=SC`

```json
{
  "ufOrigem": "RJ", "ufDestino": "SC",
  "procedimentoPadrao": true,
  "passoAPasso": "1. Consultar débitos...\n2. ...",
  "taxaEstimada": 220.00, "prazoDias": 30,
  "documentosNecessarios": "CRV original preenchido e assinado (ou ATPV-e), CNH ou RG/CPF...",
  "observacaoLegal": "Por determinação do Código de Trânsito Brasileiro...",
  "regrasEspeciaisDestino": [
    { "tipoRegra": "VISTORIA", "titulo": "Vistoria feita por empresa credenciada",
      "descricao": "Em SC a vistoria...", "fonteReferencia": "Portal do Trânsito" }
  ]
}
```

- `procedimentoPadrao: false` → instrução específica do par de UFs; `observacaoLegal` vem `null` e `regrasEspeciaisDestino` vem `[]`.
- `procedimentoPadrao: true` → não há instrução específica; vale o padrão nacional (taxa e prazo médios) mais as regras do estado de destino.

`GET /api/estados` → lista de `{ uf, nome, siglaOrgao, urlDetran, telefone, whatsapp }`.
`GET /api/estados/{uf}/servicos` → lista de `{ servico: { id, nome, descricao, categoria, disponivelOnline }, uf, taxaEstimada, prazoDias, urlServico, observacoes }`.
`GET /api/servicos` → lista de `{ id, nome, descricao, categoria, disponivelOnline }`.

### Valores possíveis

| Campo | Valores |
|---|---|
| `situacao` | `REGULAR`, `BLOQUEADO`, `SUCATA`, `FURTADO`, `ROUBADO`, `APREENDIDO` |
| `statusPagamento` | `PENDENTE`, `PAGO`, `EM_RECURSO` (pode vir `null`) |
| `tipoOcorrencia` | `FURTO`, `ROUBO`, `RECUPERACAO` |
| `tipoRegra` | `VISTORIA`, `ISENCAO_VISTORIA`, `PRAZO_DIFERENCIADO`, `TAXA_DIFERENCIADA`, `PROCESSO_DIGITAL_EXCLUSIVO`, `OUTRO` |

- Datas: `AAAA-MM-DD`.
- Valores em reais: número com casas decimais (formate no front, ex.: `Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" })`).
- Campos opcionais podem vir `null`; trate na tela (ex.: "não informado").

## Erros

Todo erro vem no formato [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (`application/problem+json`):

```json
{ "type": "about:blank", "title": "Not Found", "status": 404,
  "detail": "Nenhum veículo encontrado para a placa ABC1D23.", "instance": "/api/veiculos/ABC1D23/detalhes" }
```

| Status | Quando | O que mostrar |
|---|---|---|
| `400` | placa ou UF em formato inválido; origem igual ao destino na transferência | o `detail` (já vem em português) |
| `403` | rota inexistente na API ou origem fora do CORS | erro genérico (é bug do front ou de configuração) |
| `404` | placa não encontrada, RENAVAM ausente/errado, UF inexistente | "Veículo não encontrado. Confira a placa e o RENAVAM." |
| `429` | limite de requisições (20/min nas rotas de placa, 60/min no resto) | "Muitas consultas. Tente de novo em X s", com X do cabeçalho `Retry-After` |
| `500` | erro interno | "Erro no servidor. Tente mais tarde." |

O cabeçalho `Retry-After` (segundos) fica legível no front:

```js
if (resp.status === 429) {
  const segundos = Number(resp.headers.get("Retry-After") ?? 60);
}
```

## Segurança na tela

- `urlDetran` e `urlServico` já chegam validadas (só `http`/`https`, senão `null`): podem ir direto num `href`. Abra em nova aba e avise que é um site externo.
- Textos longos (`passoAPasso`, `documentosNecessarios`, `observacoes`, `descricao`, `observacaoLegal`) são **texto puro com quebras de linha**. Exiba como texto (ex.: CSS `white-space: pre-line`), **nunca** com `v-html`, `dangerouslySetInnerHTML` ou `innerHTML`.
- Guarde o RENAVAM só em memória durante a consulta (não em `localStorage`, URL ou log).

## Health check

`GET /actuator/health` → `{"status":"UP", ...}`. Útil para mostrar "servidor acordando" no plano gratuito do Render, que hiberna após inatividade: a primeira requisição pode levar até ~1 minuto.
