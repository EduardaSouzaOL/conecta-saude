# Backend · Minha vacinação

Microsserviço Java 21 + Spring Boot 3 (D14). Somente leitura sobre os registros de vacinação.

## Rodar

```bash
cp ../.env.example ../.env          # ajuste usuário e senha
docker compose -f ../docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # dev = carrega a base sintética
```

As variáveis do banco (`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`) são as mesmas do `.env`.

## Login no perfil dev

Enquanto o SSO do Conecta não existe, os perfis `dev` e `test` aceitam um **token falso** que diz qual pessoa sintética está logada:

```bash
curl -H "Authorization: Bearer dev-pessoa-4" http://localhost:8080/v1/eu   # Valentina, mãe do Heitor (5)
```

No perfil `dev`, o "hoje" fica fixo em **24/11/2026** (`minha-vacinacao.data-referencia`), a mesma data da base sintética e dos exemplos do contrato.

**CORS:** só as origens em `minha-vacinacao.cors.origens-permitidas` são liberadas (nenhuma por padrão; no `dev`, o Vite em `localhost:5173`). O preflight `OPTIONS` passa sem token.

A pessoa logada só vê os próprios dados e os dos dependentes (403 para qualquer outra). Fora de `dev`/`test`, nenhum token vale até o SSO real entrar em `seguranca.ValidadorToken`.

## Testes

```bash
./mvnw test
```

- Com o **Docker** rodando, os testes de banco sobem um MySQL 8.4 com Testcontainers.
- Sem Docker, dá para apontar para um MySQL vazio: `TESTE_DB_URL=jdbc:mysql://127.0.0.1:3306/banco_vazio TESTE_DB_USER=... TESTE_DB_PASSWORD=... ./mvnw test`.
- Sem nenhum dos dois, os testes de banco (API, contrato, migrações, repositórios) são **pulados** com um aviso `*** ATENÇÃO ***` no console; os testes de FHIR, da regra e de "somente leitura" rodam sempre.
- Com `CI=true` (GitHub Actions) ou `EXIGIR_BANCO=true`, a falta de banco **quebra** o build em vez de pular. O workflow `.github/workflows/backend.yml` roda a suíte completa (71 testes) com Testcontainers a cada PR.

## Pacotes

| Pacote | Dono (agente) | O que tem |
|---|---|---|
| `dados` | `dados-fhir` | Entidades JPA (`@Immutable`) e repositórios somente leitura |
| `fonte` | `dados-fhir` | `FonteRegistrosVacinacao`, `FonteBancoSintetico`, leitura e validação de `Immunization` (HAPI FHIR) |
| `regra` | `backend-java` | Regra de situação vacinal conforme as fontes oficiais (D19, docs/16), Java puro |
| `servico`, `api`, `seguranca` | `backend-java` | Serviços, endpoints do contrato, token falso (dev) e controle de acesso |

## Migrações

`src/main/resources/db/migration` (V1 a V3) e `db/dados-sinteticos` (V4, só nos perfis `dev` e `test`) são **geradas** a partir de `docs/banco-de-dados/`:

```bash
python backend/scripts/atualizar_migracoes.py
```

Não edite as migrações nem os `seed-*.sql` à mão: mude o gerador da base e rode de novo.
