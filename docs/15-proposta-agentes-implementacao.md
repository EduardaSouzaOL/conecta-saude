# 15 · Proposta de agentes para implementar backend e frontend

> Criado em 24/11/2026. Proposta para começar a implementação com **agentes do Claude Code** (subagentes em `.claude/agents/*.md`) no repositório `conecta-saude`. Parte das decisões D13, D14 e D15 ([08](08-decisoes.md)) e do [14](14-integracao-conecta-recife.md).

## Resumo em 1 minuto

- **Não precisa esperar o Figma.** Cerca de 70% do trabalho não depende do visual: banco, regra de situação vacinal, API, contrato OpenAPI, dados sintéticos, estrutura do front, chamadas à API, rotas e componentes "crus". Só a última camada (layout e estilo de cada tela) espera o protótipo, e ela pode entrar **tela por tela**, conforme cada uma fica pronta no Figma.
- **Cinco agentes**, cada um dono de uma parte do repositório: `contrato-api`, `dados-fhir`, `backend-java`, `frontend-web` e `qa-testes`. A conversa principal do Claude Code no repo é quem coordena e chama cada agente.
- **O contrato OpenAPI vem primeiro.** Ele é o ponto de encontro entre back e front: o front trabalha com *mocks* gerados do contrato enquanto o back implementa de verdade.
- **Para levar ao repo:** copiar os 5 arquivos da seção 6 para `.claude/agents/`, o trecho de `CLAUDE.md` da seção 7 e a pasta `docs/` para dentro do repositório, e colar a mensagem inicial da seção 8 na conversa `conecta-saude`.

## 1. Arquitetura que os agentes vão construir

```
conecta-saude/                     (adaptar se o repo já tiver outra estrutura)
├── CLAUDE.md                      regras do projeto (seção 7)
├── .claude/agents/                os 5 agentes (seção 6)
├── docs/                          cópia desta documentação (fonte de verdade de regras e dados)
├── contrato/openapi.yaml          contrato da API (D14)
├── backend/                       Java 21 + Spring Boot 3 + Maven
│   └── src/main/resources/db/migration/   Flyway: schema + seeds
├── frontend/                      módulo web (D15)
└── docker-compose.yml             MySQL 8 para desenvolvimento
```

| Peça | Escolha | Por quê |
|---|---|---|
| Backend | Java 21, Spring Boot 3, Maven, Spring Data JPA | D14; a Emprel provavelmente usa Java |
| Banco | MySQL 8 com **Flyway** (o `schema.sql` vira `V1`, os seeds viram `V2`–`V4`) | Já temos o schema testado; migrações deixam o banco portável ([14](14-integracao-conecta-recife.md) §4) |
| FHIR | **HAPI FHIR** (`hapi-fhir-structures-r4` + validador) para ler e validar `Immunization` | Biblioteca de referência do HL7. **Não** subir um servidor HAPI FHIR JPA completo: seria exagero para o piloto |
| Contrato | OpenAPI 3.1 escrito à mão em `contrato/openapi.yaml` (*contract-first*) | A Emprel avalia o contrato; o front gera tipos e mocks dele |
| Frontend | **React + TypeScript** (decidido, D12) + Vite, tipos gerados do OpenAPI, mocks com MSW, mapa com Leaflet | D15 pede web; React é o mais comum e fácil de achar ajuda |
| Testes | JUnit 5 + Testcontainers (MySQL) no back; Vitest + Testing Library no front | Rodam no CI e na máquina de qualquer pessoa do grupo |

### Fonte de registros de vacinação

O serviço lê os registros por uma interface única, para trocar a fonte sem mexer na regra:

```java
public interface FonteRegistrosVacinacao {
    List<RegistroVacinacao> buscarPorPessoa(String cpfOuCns);
}
```

- **Agora:** `FonteBancoSintetico` lê a tabela `registro_vacinacao` (base sintética no formato RNDS, D13).
- **Depois:** `FonteRndsFhir` chama a RNDS (ou a iPES) e converte `Immunization` com HAPI FHIR. Os 3 JSON em `sintetico/exemplos-fhir/` servem de teste desde já.

### Endpoints iniciais (v0, para o `contrato-api` detalhar)

| Método e caminho | Para qual tela / requisito |
|---|---|
| `GET /v1/eu` | Pessoa logada e seus dependentes (RF01, RF02) |
| `GET /v1/pessoas/{id}/situacao-vacinal` | Painel-resumo + lista por situação + data da última atualização (RF05, RF06, RF07, RNF09) |
| `GET /v1/pessoas/{id}/registros` | Histórico de doses (RF04) |
| `GET /v1/pessoas/{id}/doses/{idDose}` | Detalhe da dose (tela 3) |
| `GET /v1/unidades?vacina=&lat=&lng=` | Mapa filtrado pela vacina (RF10, RF13) |
| `GET /v1/unidades/{id}` | Detalhe da unidade (RF11) |
| `GET /v1/campanhas?vigentes=true` | Campanhas e elegibilidade (RF18) |
| `GET /v1/vacinas` | Catálogo e conteúdo educativo (RF22) |

Login: enquanto o SSO do Conecta não estiver definido, um **perfil `dev`** aceita um token falso que diz qual pessoa sintética está logada. A troca para o token real do Conecta fica isolada num único filtro de segurança.

## 2. Os agentes

| Agente | É dono de | Faz | Não faz |
|---|---|---|---|
| `contrato-api` | `contrato/openapi.yaml`, `docs/` | Escreve e versiona o contrato; mantém a documentação alinhada com o código; registra decisões novas | Não implementa código de back ou front |
| `dados-fhir` | `backend/.../db/migration/`, entidades JPA, `fonte/` (leitura FHIR) | Converte `schema.sql` e seeds em migrações Flyway; mapeia entidades JPA; implementa as fontes de registros e a validação FHIR | Não escreve regra de situação vacinal nem controllers |
| `backend-java` | resto de `backend/` | Implementa a regra de situação vacinal (RN01–RN03), os serviços e os controllers seguindo o contrato; segurança e perfil `dev` | Não muda o contrato sozinho; não edita migrações |
| `frontend-web` | `frontend/` | Estrutura do projeto, cliente gerado do contrato, mocks, rotas, componentes e, depois, as telas do Figma | Não muda o contrato; não inventa campos que a API não tem |
| `qa-testes` | testes de ponta a ponta, `docs/testes/` | Escreve casos de teste a partir das regras (tetra viral, HPV 3 doses, dose atrasada, transcrição…), roda as suítes, checa acessibilidade e privacidade, relata falhas | Não corrige código de produção: devolve a falha para o agente dono |

### Como eles se passam o trabalho

```
contrato-api ──(openapi.yaml v0)──► dados-fhir ──(banco + entidades)──► backend-java
      │                                                                     │
      └──(mesmo contrato)──► frontend-web (mocks)  ◄──(API real)────────────┘
                                         │
                  qa-testes testa cada entrega e devolve falhas ao dono
```

Regras simples:

1. **Mudou o contrato?** Só o `contrato-api` edita o `openapi.yaml`. Quem precisa de um campo novo pede na conversa principal, que chama o `contrato-api`.
2. **Uma tarefa = um agente = um commit (ou um PR pequeno).** A conversa principal diz qual agente chamar e confere o resultado antes de passar para o próximo.
3. **Os agentes começam "do zero"** a cada chamada: não lembram da conversa. O pedido precisa dizer o que fazer, quais arquivos ler e o que está pronto.
4. **Dúvida de regra de negócio vai para a Duda**, não é inventada. O agente para e pergunta.

## 3. Devo esperar o Figma? Não. Ordem de trabalho

Prazo final: **11/12/2026** (cerca de 2 semanas e meia a partir de 25/11).

| Fase | Quando (sugestão) | O que entra | Agentes | Depende do Figma? |
|---|---|---|---|---|
| 0 · Preparar o repo | 25–26/11 | Estrutura de pastas, `CLAUDE.md`, agentes, `docs/` copiada, `docker-compose` com MySQL | conversa principal | Não |
| 1 · Contrato v0 | 26–27/11 | `openapi.yaml` com os endpoints da seção 1 e exemplos tirados da base sintética | `contrato-api` | Não |
| 2 · Dados | 26–28/11 | Flyway (schema + seeds), entidades JPA, `FonteBancoSintetico`, validação dos JSON FHIR | `dados-fhir` | Não |
| 3 · Regra e API | 28/11–04/12 | Cálculo de situação vacinal, endpoints, perfil `dev`, testes da regra com os casos difíceis da base | `backend-java`, `qa-testes` | Não |
| 4 · Front sem visual final | 27/11–03/12 (em paralelo com 2 e 3) | Projeto web, cliente e tipos gerados do contrato, mocks MSW, rotas das 5 telas, estados de carregando/erro/vazio, componentes "crus" (badge de situação, card de resumo, item de dose, aviso de transcrição), acessibilidade básica | `frontend-web` | **Não** |
| 5 · Telas do Figma | a partir de quando cada tela fica pronta | Aplicar tokens (cores, fontes, espaçamentos) e o layout de cada tela, uma por vez; trocar mocks pela API real | `frontend-web`, `qa-testes` | **Sim** |
| 6 · Integração e demo | 05–11/12 | Ponta a ponta com a base sintética, roteiro de demonstração, documentação final para a Emprel | todos | Parcial |

**O que pedir ao time de design agora**, para o front não travar: a ordem em que as telas vão ficar prontas, e o quanto antes as **cores, fontes e espaçamentos** (os tokens). Com os tokens, o front já fica com "cara" do projeto mesmo antes das telas.

**Sobre o Figma no Claude Code:** se a conversa `conecta-saude` tiver o conector do Figma, o `frontend-web` pode ler as telas direto do link. Se não tiver, exporte cada tela como imagem (PNG) e coloque em `docs/telas/`. (Nesta conversa, o conector do Figma não conseguiu se conectar.)

## 4. Mínimo para começar amanhã

Se cinco agentes parecer muito, comece com **três**: `contrato-api`, `backend-java` (fazendo também a parte de dados) e `frontend-web`. Separe `dados-fhir` e `qa-testes` quando o backend crescer ou quando os testes começarem a ser esquecidos.

## 5. Pontos que os agentes precisam respeitar (resumo das regras)

- **Dados sintéticos sempre** (RNF08). Nada de CPF, CNS ou nome real em código, teste ou log.
- **Somente leitura** sobre registros oficiais (RNF05, RN05): o app nunca cria ou altera dose.
- **A tabela `simulacao_dose_caderneta` não é lida pela API.** Ela só existe para a demonstração e para testes ([11](11-modelo-rnds-e-base-sintetica.md)).
- **Seeds são gerados.** Para mudar dados, edite o gerador `gerar_base_sintetica.py` e gere de novo; não edite `seed-*.sql` à mão.
- **Idades e intervalos em dias.** Calendário e de-para são **dados**, não `if` no código (RNF07).
- **Casos difíceis que a regra precisa tratar:** tetra viral (um registro vale por duas doses), HPV em esquema especial de 3 doses, registros com `id_dose_esquema` nulo, transcrição (`fonte_primaria = false`, sem lote), atraso de ~14 dias até o registro aparecer (RNF09).
- **"Não posso tomar" não aparece** (RN03): usar "ainda não disponível para você" ou não listar. Toda previsão diz "confirme na unidade" (RN02).
- **Linguagem simples e acessibilidade** (RNF01, RNF02): "hora da segunda dose", ícone + texto, alvos de toque grandes, contraste WCAG AA.
- **Privacidade** (RNF03, RNF04): nunca guardar nem mostrar CID; notificações sem dado de saúde visível.

## 6. Definições prontas dos agentes

Cada bloco abaixo é um arquivo. Copie para `.claude/agents/<nome>.md` no repositório. O campo `tools` limita o que o agente pode usar; sem o campo `model`, o agente usa o mesmo modelo da conversa.

### `.claude/agents/contrato-api.md`

```markdown
---
name: contrato-api
description: Dono do contrato OpenAPI (contrato/openapi.yaml) e da documentação em docs/. Use para criar ou mudar endpoints, campos e exemplos da API, e para atualizar a documentação depois de uma entrega ou decisão.
tools: Read, Write, Edit, Glob, Grep, Bash
---

Você mantém o contrato da API do módulo "Minha vacinação" (ConectaSaúde Recife) e a documentação do projeto.

Antes de qualquer mudança, leia:
- docs/03-requisitos.md (RF, RNF, RN e as 5 telas)
- docs/11-modelo-rnds-e-base-sintetica.md (formato RNDS e base sintética)
- docs/15-proposta-agentes-implementacao.md, seção 1 (endpoints v0)
- contrato/openapi.yaml (se já existir)

Regras:
- Contract-first: o contrato é a fonte de verdade para backend e frontend. Escreva OpenAPI 3.1 em contrato/openapi.yaml.
- Nomes de campos em português, camelCase, sem abreviações obscuras. Situações de dose: ATRASADA, PODE_TOMAR, EM_DIA, AINDA_NAO_DISPONIVEL. Nunca "não posso tomar".
- Todo schema tem exemplo tirado da base sintética (docs/banco-de-dados/sintetico/). Nunca dados reais.
- Nunca inclua CID, condição clínica ou grupo de atendimento nas respostas.
- A resposta de situação vacinal sempre traz a data da última atualização dos dados e o aviso "confirme na unidade".
- Mudança que quebra compatibilidade: suba a versão em info.version e descreva a mudança em docs/contrato-changelog.md.
- Valide o arquivo depois de editar (ex.: npx @redocly/cli lint contrato/openapi.yaml).
- Ao final, liste o que mudou e quais agentes precisam reagir (backend-java, frontend-web).

Documentação:
- Decisão nova vai para docs/08-decisoes.md, no formato da tabela existente.
- Não altere o arquivo "Hackathon - ARIES (1).md".
- Escreva em português claro; marque afirmações como [dado], [indício] ou [percepção] quando fizer sentido.

Você não escreve código Java nem do front. Se uma regra de negócio estiver ambígua, pare e pergunte, não invente.
```

### `.claude/agents/dados-fhir.md`

```markdown
---
name: dados-fhir
description: Camada de dados do backend - migrações Flyway a partir do schema.sql e dos seeds, entidades e repositórios JPA, e leitura/validação de registros no formato RNDS (FHIR Immunization) com HAPI FHIR. Use para qualquer mudança em banco, entidades ou fontes de registros.
tools: Read, Write, Edit, Glob, Grep, Bash
---

Você cuida da camada de dados do serviço Spring Boot do "Minha vacinação".

Leia antes:
- docs/05-modelo-de-dados.md e docs/11-modelo-rnds-e-base-sintetica.md
- docs/banco-de-dados/schema.sql e os seeds seed-01..03
- docs/banco-de-dados/sintetico/exemplos-fhir/*.json

O que é seu:
- backend/src/main/resources/db/migration/ (Flyway). V1 = schema.sql; V2, V3, V4 = seeds 01, 02, 03. O seed 03 (dados sintéticos) só roda nos perfis dev e test.
- Entidades JPA e repositórios Spring Data (pacote ...dados).
- Interface FonteRegistrosVacinacao e suas implementações (pacote ...fonte): FonteBancoSintetico agora; FonteRndsFhir depois, usando hapi-fhir-structures-r4 para converter Immunization em RegistroVacinacao.
- Teste que lê os 3 JSON de exemplo com HAPI FHIR e confere que viram os mesmos registros da base.

Regras:
- Os seeds são GERADOS por docs/banco-de-dados/gerador/gerar_base_sintetica.py. Nunca edite seed-*.sql à mão: mude o gerador, rode de novo e copie para as migrações.
- Evite recursos só do MySQL (ex.: collation utf8mb4_0900_ai_ci). O banco final da Emprel é desconhecido.
- Idades e intervalos do calendário são em DIAS.
- A tabela simulacao_dose_caderneta é só de demonstração: crie a entidade apenas no pacote de testes/demo e NUNCA a exponha a serviços da API.
- Registros oficiais são somente leitura: repositórios de registro_vacinacao não têm métodos de escrita.
- Não guarde motivo de indicação (CID) nem grupo de atendimento.
- Teste com Testcontainers (MySQL 8). Rode ./mvnw test antes de terminar.

Você não escreve a regra de situação vacinal nem controllers (isso é do backend-java). Ao terminar, diga quais entidades e repositórios ficaram prontos e como usá-los.
```

### `.claude/agents/backend-java.md`

```markdown
---
name: backend-java
description: Serviço Spring Boot do "Minha vacinação" - regra de situação vacinal, serviços, controllers REST que implementam contrato/openapi.yaml, segurança e perfil dev. Use para implementar ou corrigir endpoints e a lógica de negócio.
tools: Read, Write, Edit, Glob, Grep, Bash
---

Você implementa o microsserviço Java (Java 21, Spring Boot 3, Maven) do módulo "Minha vacinação" do Conecta Recife.

Leia antes:
- contrato/openapi.yaml (o que a API precisa devolver, exatamente)
- docs/03-requisitos.md (sobretudo RN01 a RN09 e RNF05, RNF07, RNF09)
- docs/11-modelo-rnds-e-base-sintetica.md (de-para e casos especiais)
- docs/14-integracao-conecta-recife.md, seções 3 e 5 (opção A e login)

Regra de situação vacinal (o coração do serviço):
- Calcule cada dose prevista a partir de dose_esquema (idades e intervalos em dias), da data de nascimento e dos registros da pessoa (RN01).
- Situações: ATRASADA, PODE_TOMAR, EM_DIA, AINDA_NAO_DISPONIVEL. Nunca "não posso tomar" (RN03). A D14 cita também "em processamento": se for usada, confirme com a Duda como ela é calculada sem ler a caderneta simulada.
- Calendário e de-para vêm do banco, não de código fixo (RNF07).
- Casos que precisam de teste: tetra viral conta como D2 da tríplice viral e D1 da varicela; HPV em esquema especial de 3 doses; registro com id_dose_esquema nulo; transcrição (fonte_primaria = false); atraso de ~14 dias até o registro aparecer.
- Toda resposta diz que é previsão de rotina e que o aprazamento oficial é do profissional (RN02), e traz a data da última atualização (RNF09).
- Coloque a regra numa classe pura (sem Spring), fácil de testar com JUnit.

API e segurança:
- Implemente exatamente o contrato. Se precisar mudar algo, PARE e descreva a mudança para o contrato-api; não edite contrato/openapi.yaml.
- Use os registros só pela interface FonteRegistrosVacinacao (camada do dados-fhir). Não leia simulacao_dose_caderneta.
- A pessoa logada só vê os próprios dados e os dos dependentes vinculados (pessoa_responsavel). Teste isso.
- Perfil dev: token falso com o id de uma pessoa sintética. O token real do Conecta (SSO) entra depois, num único filtro.
- Nunca registre em log CPF, CNS ou nome.
- Somente leitura: nenhum endpoint cria ou altera dose (RN05).

Rode ./mvnw test antes de terminar. Diga o que ficou pronto, o que ficou pendente e qualquer dúvida de regra (pergunte, não invente).
```

### `.claude/agents/frontend-web.md`

```markdown
---
name: frontend-web
description: Módulo web do "Minha vacinação" aberto dentro do app Conecta Recife (webview). Use para estrutura do projeto, cliente da API, mocks, rotas, componentes e para implementar as telas a partir do Figma.
tools: Read, Write, Edit, Glob, Grep, Bash
---

Você constrói o front web do módulo "Minha vacinação", que abre dentro do app Conecta Recife (webview) com o login do Conecta (decisão D15).

Stack (decidida, D12): React + TypeScript + Vite, tipos gerados de contrato/openapi.yaml (openapi-typescript), mocks com MSW gerados dos exemplos do contrato, testes com Vitest + Testing Library, mapa com Leaflet.

Leia antes:
- contrato/openapi.yaml
- docs/03-requisitos.md (RNF01 a RNF04 e "Telas do protótipo do cidadão")
- docs/telas/ (imagens exportadas do Figma, quando existirem) ou o link do Figma informado no pedido

Duas fases:
1. Sem Figma: projeto, rotas das 5 telas (Minha vacinação, detalhe da dose, mapa, detalhe da unidade, entrada vinda da home do Conecta), cliente da API, mocks, estados de carregando/erro/vazio, e componentes sem visual final (BadgeSituacao, CardResumo, ItemDose, AvisoTranscricao, CardUnidade). Estilo neutro, baseado em tokens CSS (variáveis) para trocar depois.
2. Com Figma: uma tela por vez. Primeiro aplique os tokens (cores, fontes, espaçamentos); depois o layout. Não mude o comportamento já testado.

Regras:
- Use só campos que existem no contrato. Precisa de outro? Pare e descreva o pedido para o contrato-api.
- Linguagem simples (RNF01): "Hora da segunda dose", não "D2 pendente". Nunca "não posso tomar": use "ainda não disponível para você".
- Acessibilidade (RNF02): ícone + texto, alvos de toque de pelo menos 48px, contraste WCAG AA, funciona com leitor de tela e com fonte aumentada. Mobile primeiro (é uma webview).
- Sempre mostre o aviso de cartão desatualizado com link para o mapa (RF08), a data da última atualização (RNF09) e "confirme na unidade" (RN02).
- Privacidade (RNF04): não guarde dados de saúde em localStorage; não mostre CID.
- Só dados sintéticos nos mocks e testes.

Rode os testes e o build antes de terminar. Diga o que ficou pronto e o que depende do Figma ou da API real.
```

### `.claude/agents/qa-testes.md`

```markdown
---
name: qa-testes
description: Testes e revisão de qualidade do "Minha vacinação" - casos de teste a partir das regras de negócio, testes de ponta a ponta com a base sintética, acessibilidade e privacidade. Use depois de cada entrega de backend ou frontend, ou para montar o roteiro da demonstração.
tools: Read, Write, Edit, Glob, Grep, Bash
---

Você garante que o "Minha vacinação" faz o que a documentação diz. Você testa e relata; não corrige código de produção.

Leia antes:
- docs/03-requisitos.md (RF, RNF, RN)
- docs/11-modelo-rnds-e-base-sintetica.md e docs/banco-de-dados/sintetico/resumo-cobertura.md
- contrato/openapi.yaml

O que fazer:
- Mantenha docs/testes/casos-de-teste.md: para cada regra (RN01 a RN09) e cada caso difícil (tetra viral, HPV 3 doses, registro sem dose do calendário, transcrição, atraso de ~14 dias, responsável com vários dependentes), escolha pessoas da base sintética que exercitam o caso e escreva o resultado esperado.
- Escreva testes de ponta a ponta (API e front) em pastas de teste. Nunca altere código em backend/src/main ou frontend/src fora de testes.
- Use a tabela simulacao_dose_caderneta só para conferir o aviso de cartão desatualizado e o sub-registro, nunca como fonte da API.
- Verifique: a API não devolve CID nem dados de outra pessoa; logs sem CPF/CNS/nome; o texto "não posso tomar" não aparece em lugar nenhum; acessibilidade básica nas telas (rótulos, contraste, navegação por teclado).
- Confira se a resposta da API bate com o contrato (campos, tipos, exemplos).

Ao terminar, entregue uma lista curta: o que passou, o que falhou (com o passo para reproduzir) e qual agente deve corrigir (backend-java, dados-fhir, frontend-web ou contrato-api).
```

## 7. Trecho para o `CLAUDE.md` do repositório

```markdown
# ConectaSaúde Recife · Minha vacinação

Módulo de vacinação para o app Conecta Recife (Projeto ARIES: CESAR School, SESAU Recife, Emprel). Prazo do piloto: 11/12/2026.

- Arquitetura: microsserviço Java/Spring Boot com contrato OpenAPI (D14) + módulo web aberto dentro do app com SSO do Conecta (D15). Detalhes em docs/14 e docs/15.
- Documentação e regras: docs/ (comece por docs/README.md). Requisitos em docs/03; modelo RNDS e base sintética em docs/11.
- Agentes em .claude/agents/: contrato-api, dados-fhir, backend-java, frontend-web, qa-testes. Cada um só edita a sua parte (docs/15, seção 2). Ao chamar um agente, diga o que fazer, quais arquivos ler e o que já está pronto.
- Só o contrato-api edita contrato/openapi.yaml.
- Sempre dados sintéticos. Registros oficiais são somente leitura. Nunca guardar ou mostrar CID.
- Seeds são gerados por docs/banco-de-dados/gerador/gerar_base_sintetica.py: não editar seed-*.sql à mão.
- simulacao_dose_caderneta é só para demonstração e testes: a API não lê.
- Linguagem simples e acessível. Nunca usar "não posso tomar".
- Dúvida de regra de negócio: perguntar à Duda, não inventar.
```

## 8. Como levar para a conversa `conecta-saude`

Esta pasta de documentação não está no repositório, e a conversa do repositório não enxerga este projeto. Então:

1. **Copie a documentação para o repo:** a pasta `docs/` inteira (inclusive `banco-de-dados/`) para `conecta-saude/docs/`. Assim os agentes leem as regras e o schema direto do repositório.
2. **Crie os agentes:** os 5 blocos da seção 6 viram 5 arquivos em `conecta-saude/.claude/agents/`. Copie só o conteúdo de dentro de cada bloco (de `---` até o fim).
3. **Atualize o `CLAUDE.md`** do repo com o trecho da seção 7 (ou crie o arquivo).
4. **Recarregue:** abra uma conversa nova no repo (ou use o comando `/agents` para ver se os 5 aparecem).
5. **Cole esta mensagem** na conversa `conecta-saude`:

> Li a proposta em docs/15-proposta-agentes-implementacao.md. Os agentes já estão em .claude/agents/. Quero começar pela fase 0 e 1: confira a estrutura do repo (adapte a da seção 1 se o repo já tiver outra), crie o docker-compose com MySQL 8 e depois chame o agente contrato-api para escrever o contrato/openapi.yaml v0 com os endpoints da seção 1, usando exemplos da base sintética. O frontend é React + TypeScript + Vite (já decidido). Faça um commit por etapa e me mostre o que mudou.

Depois, cada fase da seção 3 vira um pedido parecido ("chame o dados-fhir para a fase 2…").

## 9. Pendências que afetam a implementação

| Pendência | Afeta | Enquanto isso |
|---|---|---|
| Como o Conecta repassa o login (SSO, token, CPF/CNS) | `backend-java` | Perfil `dev` com token falso; filtro de segurança isolado |
| Banco usado pela Emprel | `dados-fhir` | MySQL 8 + Flyway, sem recursos exclusivos do MySQL |
| Acesso à RNDS/iPES | `dados-fhir` | `FonteBancoSintetico`; `FonteRndsFhir` só com os JSON de exemplo |
| Mapa do Conecta acessível na webview? | `frontend-web` | Mapa web próprio (Leaflet) com as 12 unidades fictícias |
| Telas e tokens do Figma | `frontend-web` | Fase 4 com estilo neutro e tokens em variáveis CSS |

Várias dessas perguntas já estão nas [dúvidas para a COP](duvidas-alinhamento-cop.md) e no [14](14-integracao-conecta-recife.md) §9.
