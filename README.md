<div align="center">

# 🤖👨‍🍳 MagicChefAI

**Seu chef particular movido a IA generativa.** Cadastre o que tem na geladeira e no armário, converse com o Chef Magic e receba receitas completas em segundos.

[![CI/CD](https://github.com/lucasaita1/MagicChefAI/actions/workflows/ci.yml/badge.svg)](https://github.com/lucasaita1/MagicChefAI/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6DB33F)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![Postgres](https://img.shields.io/badge/Postgres-16-336791)

🌐 **Produção:** _a definir após o deploy_ · 🎬 **Vídeo:** _a definir_

</div>

---

## 📌 O problema

Todo mundo já abriu a geladeira e pensou *"o que eu faço com isso?"*. Ingredientes vencem, a criatividade acaba e buscar receita por receita na internet não leva em conta **o que você realmente tem em casa**.

## 💡 Por que IA generativa?

Receitas fixas não se adaptam a combinações arbitrárias de ingredientes, restrições ("sem lactose", "rápido", "vegetariano") e quantidades. Um modelo de linguagem consegue **criar** uma receita nova, coerente e explicada passo a passo para exatamente aquela combinação. O MagicChefAI usa o **ChatGPT (OpenAI)** via **Spring AI** com saída estruturada (JSON Schema), para que a resposta sempre chegue no formato da tela.

## ✨ Funcionalidades

- 🔐 **Login e cadastro** com JWT + cookie de fingerprint HttpOnly. Logout invalida o token na hora
- 🧊 **Minha geladeira & meu armário**: CRUD de ingredientes com categoria, quantidade, unidade e validade (alerta de vencimento)
- 💬 **Chat com o Chef Magic**: o chef pergunta os ingredientes, você digita e/ou carrega da despensa, e ele gera a receita
- 📖 **Minhas receitas**: histórico de tudo que foi gerado, com busca, favoritos e detalhe completo
- 🛡️ **Proteção contra prompt injection** em 5 camadas: a IA só fala de culinária
- 🤖 Interface premium com **chef robô animado em SVG** (acena, pensa, comemora, fica confuso)

## 🏗️ Arquitetura

```mermaid
flowchart LR
    U[Usuário] --> V[Vue 3 + Vite<br/>Vercel]
    V -- "/api (proxy same-origin)" --> A[Spring Boot 4 API<br/>Render · Docker]
    A -- Flyway + JPA --> P[(Postgres 16)]
    A -- Spring AI ChatClient --> O[OpenAI<br/>gpt-4o-mini]
```

**Pipeline de geração de receita** (`POST /api/recipes/generate`):

```mermaid
flowchart LR
    R[Request] --> S{Spring Security<br/>JWT + fingerprint + sessão ativa}
    S -- 401 --> X1[Bloqueado]
    S --> L{Rate limit<br/>por usuário}
    L -- 429 --> X2[Bloqueado]
    L --> G{PromptGuard<br/>sanitiza + detecta injection}
    G -- 400 --> X3[Bloqueado sem chamar a IA]
    G --> PF[Prompt com delimitadores<br/>+ canário secreto]
    PF --> AI[OpenAI]
    AI --> VO{Validação da saída<br/>schema · canário · offTopic}
    VO -- 422/502 --> X4[Descartada]
    VO --> DB[(Salva em Minhas receitas)]
```

## 🛡️ Segurança

### Autenticação (Spring Security + Nimbus JOSE)

| Ameaça | Defesa |
|---|---|
| Acesso à IA sem login | Todas as rotas exceto login/cadastro/saúde/docs exigem autenticação |
| Token usado após logout | Cada JWT tem `jti` ligado a uma linha em `user_sessions`. O logout revoga a sessão e o token morre na hora |
| **Token roubado (XSS, log, rede)** | Padrão OWASP *token sidejacking*: o JWT carrega apenas o SHA-256 de um valor aleatório que fica num cookie **HttpOnly + Secure + SameSite=Strict**. Sem o cookie, o token é inútil, e o uso indevido **queima a sessão** |
| Força bruta no login | Rate limit por IP + e-mail, BCrypt e mensagem genérica (`E-mail ou senha inválidos.`) |
| Enumeração de dados | Toda consulta é filtrada pelo dono; o recurso de outro usuário retorna 404 |

### Prompt injection: defesa em camadas

1. **Validação de entrada**: tamanho máximo, regex nos nomes de ingredientes, Bean Validation
2. **Sanitização** (`PromptGuard.sanitize`): normalização Unicode NFKC (anula caracteres "disfarçados"), remove caracteres invisíveis e símbolos de marcação (`< > { } [ ] \``…), para o usuário não conseguir fechar as tags do prompt
3. **Detecção** (`PromptGuard.inspect`): padrões em PT/EN como "ignore as instruções", "revele seu prompt", "aja como", "modo desenvolvedor", `role: system`… A requisição é **barrada antes de gastar um token**
4. **Prompt blindado** (`RecipePromptFactory`): o conteúdo do usuário vai entre `<pedido_usuario>` e é declarado como *dado não confiável*; system e user ficam em mensagens separadas; o modelo pode responder `offTopic: true`
5. **Validação da saída** (`OpenAiRecipeClient` + `RecipeService`): JSON Schema obrigatório, **canário secreto** (se o código aparecer na resposta, ela é descartada), campos obrigatórios e truncamento/clamp de valores

> Chaves de API **nunca** vão para o código: `.env` fica no `.gitignore` e o pipeline roda o **gitleaks** sobre todo o histórico a cada push.

## 🔌 Endpoints

Documentação interativa: `http://localhost:8080/swagger-ui.html`

| Método | Rota | Auth | Descrição |
|---|---|:---:|---|
| `GET` | `/api/health` | – | **Saúde** da API e do banco |
| `POST` | `/api/auth/register` | – | Cria conta e inicia sessão |
| `POST` | `/api/auth/login` | – | Login (JWT + cookie de fingerprint) |
| `POST` | `/api/auth/logout` | ✅ | Revoga a sessão imediatamente |
| `GET` | `/api/auth/me` | ✅ | Usuário autenticado |
| `POST` | `/api/recipes/generate` | ✅ | **Pergunta à IA**: gera receita a partir dos ingredientes |
| `GET` | `/api/recipes` | ✅ | Minhas receitas |
| `GET` | `/api/recipes/{id}` | ✅ | Detalhe |
| `PATCH` | `/api/recipes/{id}/favorite` | ✅ | Favoritar/desfavoritar |
| `DELETE` | `/api/recipes/{id}` | ✅ | Excluir |
| `GET` | `/api/pantry?location=FRIDGE\|CUPBOARD` | ✅ | Geladeira/armário |
| `POST` / `PUT` / `DELETE` | `/api/pantry[/{id}]` | ✅ | CRUD de ingredientes |

## 📁 Estrutura

```
MagicChefAI/
├── backend/                     # Spring Boot 4 · Java 21
│   ├── src/main/java/dev/lucas/magicchef/
│   │   ├── ai/                  # PromptGuard, prompt factory, cliente OpenAI (Spring AI)
│   │   ├── auth/                # registro, login, logout, sessões
│   │   ├── security/            # filtro JWT, fingerprint, SecurityConfig
│   │   ├── pantry/              # geladeira & armário
│   │   ├── recipe/              # geração e histórico de receitas
│   │   ├── user/                # entidades de usuário e sessão
│   │   ├── common/              # health, rate limit, erros (Problem Details)
│   │   └── config/              # properties tipadas, OpenAPI, Clock
│   ├── src/main/resources/db/migration/   # Flyway V1..V5
│   └── src/test/                # unitários + integração (H2) com IA mockada
├── frontend/                    # Vue 3 · Vite · Pinia · Vue Router
│   ├── src/{components,views,stores,services,router,utils}
│   └── tests/                   # Vitest + Vue Test Utils
├── .github/workflows/ci.yml     # CI/CD
├── docker-compose.yaml          # Postgres 16 local
├── render.yaml                  # Blueprint de deploy da API
├── .env.example
├── DECISOES.md
└── README.md
```

## 🚀 Rodando localmente

**Pré-requisitos:** Java 21, Node 22+, Docker.

```bash
cp .env.example .env   # preencha OPENAI_API_KEY e JWT_SECRET
docker compose up -d   # Postgres 16 na porta 5433
```

```bash
cd backend && ./mvnw spring-boot:run      # API em http://localhost:8080
```

```bash
cd frontend && npm install && npm run dev # App em http://localhost:5173
```

O Flyway cria todas as tabelas na primeira execução. O Vite faz proxy de `/api` para a API, então o cookie de segurança funciona como first-party.

## 🧪 Testes e cobertura

A chamada à OpenAI é **sempre mockada** nos testes: o `RecipeAiClient` é um `@MockitoBean` nos testes de integração, e o `ChatModel` do Spring AI é um mock Mockito no teste do cliente. Nenhuma chave real é necessária.

```bash
cd backend && ./mvnw verify            # 84 testes · JaCoCo (gate de 70%)
```

```bash
cd frontend && npm run test:coverage   # 54 testes · Vitest (gate de 70%)
```

| Camada | Testes | Cobertura de linhas |
|---|---:|---:|
| Backend | 84 | **97,7%** |
| Frontend | 54 | **96,0%** |

Relatórios: `backend/target/site/jacoco/index.html` e `frontend/coverage/index.html`.

## ⚙️ CI/CD

O workflow [`ci.yml`](.github/workflows/ci.yml) roda a cada push e PR na `main`:

1. **Secret scan**: gitleaks varre todo o histórico em busca de chaves
2. **Backend**: `mvn verify` com testes, cobertura e gate de 70% (o build falha abaixo disso)
3. **Frontend**: Vitest com cobertura e thresholds de 70% + build de produção
4. **Deploy**: com os três verdes, dispara o deploy hook do Render (API); o Vercel publica o front pela integração com o GitHub

## ☁️ Deploy

**API + banco (Render):** *New → Blueprint* apontando para este repositório. O `render.yaml` cria o Postgres 16 e o serviço Docker. Preencha `OPENAI_API_KEY` e `CORS_ALLOWED_ORIGINS` no painel e copie o *Deploy Hook* para o secret `RENDER_DEPLOY_HOOK_URL` do GitHub.

**Front (Vercel):** importe o repositório com *Root Directory* `frontend`. Ajuste em [`frontend/vercel.json`](frontend/vercel.json) a URL da API no rewrite `/api/:path*`, assim o navegador fala só com o domínio do Vercel (cookie first-party e `SameSite=Strict`).

## 🔑 Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DATABASE_URL` *ou* `DB_HOST`/`DB_PORT`/`DB_NAME` | Conexão JDBC com o Postgres |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciais do banco |
| `OPENAI_API_KEY` | Chave da OpenAI |
| `OPENAI_MODEL` | Modelo (padrão `gpt-4o-mini`) |
| `JWT_SECRET` | Segredo HS256 (mín. 32 caracteres; a API não sobe sem ele) |
| `JWT_TTL` | Validade do token (padrão `2h`) |
| `COOKIE_SECURE` / `COOKIE_SAME_SITE` | Flags do cookie de fingerprint |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas |
| `AI_RATE_LIMIT` / `LOGIN_RATE_LIMIT` | Requisições por minuto |

## 📄 Licença

[MIT](LICENSE) · Feito por **Lucas Aita** para a disciplina da PUCPR.
