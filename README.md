# ConectaSaúde Recife · Minha vacinação

Módulo **"Minha vacinação"** para o app **Conecta Recife**, feito no **Projeto ARIES** (CESAR School, Secretaria de Saúde do Recife (SESAU) e Emprel), pela equipe **Biobits**. Piloto previsto para **11/12/2026**.

> ⚠️ **Todos os dados deste repositório são sintéticos.** Nenhuma pessoa, CPF, CNS ou registro de vacina é real. A base existe só para demonstração e testes.

## O que é

Hoje o cidadão não tem um jeito simples de saber quais vacinas já tomou e quais faltam. O "Minha vacinação" mostra, dentro do Conecta Recife:

- a **situação de cada dose** do calendário: **atrasada**, **já pode tomar**, **em dia** ou **ainda não disponível**;
- o **mapa das unidades de saúde** com sala de vacina;
- o **aviso de cartão desatualizado**, para quem tomou uma vacina que ainda não aparece no sistema.

**Escopo do piloto:** só a parte do **cidadão** (e dos dependentes dele), **somente leitura** (o app nunca cria nem altera uma dose) e com **dados sintéticos**.

## Arquitetura

| Decisão | O que é | Status |
|---|---|---|
| **D14 · opção A** | Microsserviço **Java / Spring Boot** com contrato **OpenAPI**. Lê registros no formato da **RNDS** (FHIR `Immunization`, com HAPI FHIR) | Decidido |
| **D15 · opção B** | Telas como **módulo web aberto dentro do app** (webview), com o login do Conecta (SSO) | Decidido |
| **D16 · opção C** | Telas nativas no app, usando a mesma API | Só proposta, para depois do piloto |

```
App Conecta Recife
  └─ webview: módulo web "Minha vacinação" (React + TypeScript)   ← frontend (a fazer)
        │  HTTPS + token do Conecta (SSO)
        ▼
  API REST "Minha vacinação" (Spring Boot)  ←  contrato/openapi.yaml
        │  somente leitura
        ▼
  MySQL: calendário, unidades e registros no formato RNDS (base sintética no piloto)
```

Detalhes em [docs/14](docs/14-integracao-conecta-recife.md) e [docs/15](docs/15-proposta-agentes-implementacao.md).

## Stack

- **Backend:** Java 21, Spring Boot 3.5.16 (web, Data JPA), Flyway, MySQL 8, HAPI FHIR 8.12.1 (R4)
- **Testes:** JUnit 5, Testcontainers (MySQL 8.4), validação do contrato com json-schema-validator
- **Frontend (decidido, D12):** React + TypeScript + Vite

## Estrutura de pastas

```
.
├── backend/              microsserviço Spring Boot (código, migrações, testes)
│   ├── scripts/          atualizar_migracoes.py: gera as migrações a partir de docs/banco-de-dados
│   └── src/
├── contrato/
│   └── openapi.yaml      contrato da API (fonte de verdade para backend e frontend)
├── docs/                 documentação do projeto (comece por docs/README.md)
│   └── banco-de-dados/   schema.sql, seeds, gerador da base sintética, referências oficiais
├── .claude/agents/       agentes do Claude Code usados na implementação
├── docker-compose.yml    MySQL de desenvolvimento
└── .env.example          modelo das variáveis do banco
```

A pasta `frontend/` ainda não existe: veja [Status e próximos passos](#status-e-próximos-passos).

## Como rodar

**Pré-requisitos:** Java 21, Docker (para o MySQL e para os testes) e Python 3 (só para regenerar a base ou as migrações).

1. **Variáveis do banco:** copie `.env.example` para `.env` e troque as senhas. O `.env` não vai para o git.

   ```bash
   cp .env.example .env
   ```

2. **Banco de desenvolvimento:**

   ```bash
   docker compose up -d
   ```

3. **Backend com a base sintética** (perfil `dev`). Rode da pasta `backend/`, com as mesmas variáveis do `.env` no ambiente (`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`):

   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   As migrações Flyway criam o schema e carregam a base sintética. A API sobe em `http://localhost:8080`.

4. **Chamar a API.** Enquanto o SSO do Conecta não existe, o perfil `dev` aceita um token falso que diz qual pessoa sintética está logada:

   ```bash
   curl -H "Authorization: Bearer dev-pessoa-4" http://localhost:8080/v1/eu
   curl -H "Authorization: Bearer dev-pessoa-4" http://localhost:8080/v1/pessoas/5/situacao-vacinal
   ```

   A pessoa 4 (Valentina) vê os próprios dados e os do filho Heitor (pessoa 5). Outra pessoa devolve **403**; sem token, **401**.

### Testes

```bash
cd backend
./mvnw test
```

- Com o **Docker rodando**, os testes de banco sobem um MySQL 8.4 com Testcontainers.
- Sem Docker, dá para apontar para um MySQL **vazio**: `TESTE_DB_URL=jdbc:mysql://127.0.0.1:3306/banco_vazio TESTE_DB_USER=... TESTE_DB_PASSWORD=... ./mvnw test`.
- Sem nenhum dos dois, os testes de banco são pulados.

### Lint do contrato

```bash
npx @redocly/cli lint contrato/openapi.yaml
```

Hoje: sem erros, com um aviso esperado sobre o servidor `localhost`.

## Contrato da API

O contrato está em [`contrato/openapi.yaml`](contrato/openapi.yaml) (**versão 0.4.1**). Todos os exemplos vêm da base sintética, com data de referência 24/11/2026. Histórico de versões em [docs/contrato-changelog.md](docs/contrato-changelog.md).

| Endpoint | O que devolve |
|---|---|
| `GET /v1/eu` | Pessoa logada e seus dependentes |
| `GET /v1/pessoas/{idPessoa}/situacao-vacinal` | Painel-resumo e doses previstas por situação |
| `GET /v1/pessoas/{idPessoa}/registros` | Histórico de doses registradas |
| `GET /v1/pessoas/{idPessoa}/doses/{idDose}` | Detalhe de uma dose prevista |
| `GET /v1/unidades` | Unidades de saúde para o mapa |
| `GET /v1/unidades/{idUnidade}` | Detalhe da unidade |
| `GET /v1/campanhas` | Campanhas de vacinação |
| `GET /v1/vacinas` | Catálogo de vacinas |

## Base sintética

> ⚠️ **Dados 100% sintéticos, só para demonstração.** Nenhuma pessoa é real.

- Fica em [`docs/banco-de-dados/`](docs/banco-de-dados/): `schema.sql`, `seed-01` (terminologias da RNDS), `seed-02` (catálogo e calendário), `seed-03` (pessoas e registros) e CSVs/exemplos FHIR em `sintetico/`.
- Os seeds são **gerados** por `docs/banco-de-dados/gerador/gerar_base_sintetica.py` (resultado sempre igual para a mesma semente). **Não edite os `seed-*.sql` à mão:** mude o gerador e rode de novo.
- As migrações do backend são geradas a partir desses arquivos com `python backend/scripts/atualizar_migracoes.py`.
- A tabela `simulacao_dose_caderneta` (a "caderneta física") é **só para demonstração e testes**: a API não lê essa tabela.

Modelo e decisões da base em [docs/11](docs/11-modelo-rnds-e-base-sintetica.md).

## Regras de vacinação

A situação de cada dose segue **fielmente as fontes oficiais** (decisão D19):

- **"Regras de entrada de dados 4"** do PNI (faixa etária, intervalo mínimo e sequência das doses);
- **Calendário Nacional de Vacinação 2026** (idade recomendada).

Cada idade e intervalo está no banco, com a fonte citada. A regra completa, as escolhas de aplicação e os pontos que ainda precisam ser confirmados com a SESAU estão em [docs/16](docs/16-regra-situacao-vacinal.md).

Enquanto a SESAU não confirma tudo, o app sempre mostra os avisos "Estas regras ainda estão em alinhamento com a Secretaria de Saúde. Confirme sempre na unidade." e "Uma vacina tomada pode levar até 14 dias para aparecer aqui.".

## Agentes do Claude Code

A implementação usa 5 agentes em [`.claude/agents/`](.claude/agents/). Cada um só mexe na sua parte ([docs/15](docs/15-proposta-agentes-implementacao.md), seção 2):

| Agente | O que faz |
|---|---|
| `contrato-api` | Dono do `contrato/openapi.yaml` e da documentação em `docs/` |
| `dados-fhir` | Migrações, entidades e repositórios JPA, leitura de registros RNDS (FHIR) |
| `backend-java` | Regra de situação vacinal, serviços, endpoints, segurança e perfil dev |
| `frontend-web` | Módulo web aberto dentro do app (estrutura, cliente da API, telas) |
| `qa-testes` | Casos de teste, testes de ponta a ponta, acessibilidade e privacidade |

## Status e próximos passos

- ✅ **Backend e contrato prontos para o piloto:** 8 endpoints, regra oficial de situação vacinal e **71 testes passando**.
- 🔜 **Frontend:** vai ser feito por outra pessoa da equipe. A pasta `frontend/` está vazia de propósito. O ponto de partida é o [contrato](contrato/openapi.yaml), com mocks a partir dos exemplos dele, e as instruções em [`.claude/agents/frontend-web.md`](.claude/agents/frontend-web.md).
- ⏳ **Pontos da regra a confirmar com a SESAU:** lista em [docs/16](docs/16-regra-situacao-vacinal.md).
- ⏳ **Login real (SSO do Conecta)** e **acesso à RNDS:** dependem da Emprel e do Ministério da Saúde ([docs/09](docs/09-proximos-passos-e-pendencias.md)).

## Documentação

Comece por [docs/README.md](docs/README.md). Os principais:

- [01 · Visão geral e problema](docs/01-visao-geral-e-problema.md)
- [03 · Requisitos](docs/03-requisitos.md)
- [08 · Decisões](docs/08-decisoes.md)
- [11 · Modelo RNDS e base sintética](docs/11-modelo-rnds-e-base-sintetica.md)
- [14 · Integração com o Conecta Recife](docs/14-integracao-conecta-recife.md)
- [15 · Agentes para implementação](docs/15-proposta-agentes-implementacao.md)
- [16 · Regra de situação vacinal](docs/16-regra-situacao-vacinal.md)

## Privacidade (LGPD)

- Só **dados sintéticos** no repositório e nos testes.
- A API **nunca** devolve nem guarda CID, condição clínica, CPF, CNS ou dados do profissional que aplicou a dose.
- Os logs não levam nome, CPF nem CNS.
- Registros oficiais são **somente leitura**.
